package us.shandian.giga.postprocessing;

import android.os.Message;

import org.schabi.newpipe.streams.io.SharpStream;

import java.io.File;
import java.io.IOException;

import us.shandian.giga.get.DownloadMission;
import us.shandian.giga.postprocessing.io.ChunkFileInputStream;
import us.shandian.giga.postprocessing.io.CircularFile;
import us.shandian.giga.service.DownloadManagerService;

public abstract class Postprocessing {

    static final byte OK_RESULT = DownloadMission.ERROR_NOTHING;

    public static final String ALGORITHM_TTML_CONVERTER = "ttml";
    public static final String ALGORITHM_MP4_DASH_MUXER = "mp4D";
    public static final String ALGORITHM_WEBM_MUXER = "webm";
    private static final String ALGORITHM_TEST_ALGO = "test";

    public static Postprocessing getAlgorithm(String algorithmName, DownloadMission mission) {
        if (null == algorithmName) {
            throw new NullPointerException("algorithmName");
        } else switch (algorithmName) {
            case ALGORITHM_TTML_CONVERTER:
                return new TttmlConverter(mission);
            case ALGORITHM_MP4_DASH_MUXER:
                return new Mp4DashMuxer(mission);
            case ALGORITHM_WEBM_MUXER:
                return new WebMMuxer(mission);
            case ALGORITHM_TEST_ALGO:
                return new TestAlgo(mission);
            /*case "example-algorithm":
            return new ExampleAlgorithm(mission);*/
            default:
                throw new RuntimeException("Unimplemented post-processing algorithm: " + algorithmName);
        }
    }

    /**
     * Get a boolean value that indicate if the given algorithm work on the same
     * file
     */
    public boolean worksOnSameFile;

    /**
     * Get the recommended space to reserve for the given algorithm. The amount
     * is in bytes
     */
    public int recommendedReserve;

    protected DownloadMission mission;

    Postprocessing(DownloadMission mission) {
        this.mission = mission;
    }

    public void run() throws IOException {
        File file = mission.getDownloadedFile();
        CircularFile out = null;
        ChunkFileInputStream[] sources = new ChunkFileInputStream[mission.urls.length];

        try {
            int i = 0;
            for (; i < sources.length - 1; i++) {
                sources[i] = new ChunkFileInputStream(file, mission.offsets[i], mission.offsets[i + 1], "rw");
            }
            sources[i] = new ChunkFileInputStream(file, mission.offsets[i], mission.getDownloadedFile().length(), "rw");

            int[] idx = {0};
            CircularFile.OffsetChecker checker = () -> {
                while (idx[0] < sources.length) {
                    /*
                     * WARNING: never use rewind() in any chunk after any writing (especially on first chunks)
                     *          or the CircularFile can lead to unexpected results
                     */
                    if (sources[idx[0]].isDisposed() || sources[idx[0]].available() < 1) {
                        idx[0]++;
                        continue;// the selected source is not used anymore
                    }

                    return sources[idx[0]].getFilePointer() - 1;
                }

                return -1;
            };

            out = new CircularFile(file, 0, this::progressReport, checker);

            mission.done = 0;
            mission.length = file.length();

            int result = process(out, sources);

            if (result == OK_RESULT) {
                long finalLength = out.finalizeFile();
                mission.done = finalLength;
                mission.length = finalLength;
            } else {
                mission.errCode = DownloadMission.ERROR_UNKNOWN_EXCEPTION;
                mission.errObject = new RuntimeException("post-processing algorithm returned " + result);
            }

            if (result != OK_RESULT && worksOnSameFile) {
                //noinspection ResultOfMethodCallIgnored
                new File(mission.location, mission.name).delete();
            }
        } finally {
            for (SharpStream source : sources) {
                if (source != null && !source.isDisposed()) {
                    source.dispose();
                }
            }
            if (out != null) {
                out.dispose();
            }
        }
    }

    /**
     * Abstract method to execute the pos-processing algorithm
     *
     * @param out     output stream
     * @param sources files to be processed
     * @return a error code, 0 means the operation was successful
     * @throws IOException if an I/O error occurs.
     */
    abstract int process(SharpStream out, SharpStream... sources) throws IOException;

    String getArgumentAt(int index, String defaultValue) {
        if (mission.postprocessingArgs == null || index >= mission.postprocessingArgs.length) {
            return defaultValue;
        }

        return mission.postprocessingArgs[index];
    }

    private void progressReport(long done) {
        mission.done = done;
        if (mission.length < mission.done) mission.length = mission.done;

        Message m = new Message();
        m.what = DownloadManagerService.MESSAGE_PROGRESS;
        m.obj = mission;

        mission.mHandler.sendMessage(m);
    }
}
