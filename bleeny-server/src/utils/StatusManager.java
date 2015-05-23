package utils;

import java.io.InputStreamReader;
import java.io.LineNumberReader;

/**
 * Created by JeffreyZhang on 2014/6/24.
 */
public class StatusManager {

    private static final int CPUTIME = 5000;
    private static final int PERCENT = 100;
    private static final int FAULTLENGTH = 10;
    private static final int KB = 1024;

    /**
     * 可使用内存.
     */
    private long totalMemory;
    /**
     * 剩余内存.
     */
    private long freeMemory;
    /**
     * 最大可使用内存.
     */
    private long maxMemory;
    /**
     * 操作系统.
     */
    private String osName;
//    /** 总的物理内存. */
//    private long totalMemorySize;
//    /** 剩余的物理内存. */
//    private long freePhysicalMemorySize;
//    /** 已使用的物理内存. */
//    private long usedMemory;
    /**
     * 线程总数.
     */
    private int totalThread;
    /**
     * cpu使用率.
     */
    private double cpuRatio;

    public long getFreeMemory() {
        return freeMemory;
    }

//    public long getFreePhysicalMemorySize() {
//        return freePhysicalMemorySize;
//    }

    public long getMaxMemory() {
        return maxMemory;
    }

    public String getOsName() {
        return osName;
    }

    public long getTotalMemory() {
        return totalMemory;
    }

//    public long getTotalMemorySize() {
//        return totalMemorySize;
//    }

    public int getTotalThread() {
        return totalThread;
    }

//    public long getUsedMemory() {
//        return usedMemory;
//    }

    public double getCpuRatio() {
        return cpuRatio;
    }

    public StatusManager() {
        init();
    }

    public void init() {
        Runtime runtime = Runtime.getRuntime();
        totalMemory = runtime.totalMemory() / KB;
        freeMemory = runtime.freeMemory() / KB;
        maxMemory = runtime.maxMemory() / KB;
        osName = System.getProperty("os.name");

        ThreadGroup parentThread;
        for (parentThread = Thread.currentThread().getThreadGroup(); parentThread
                .getParent() != null; parentThread = parentThread.getParent())
            ;
        totalThread = parentThread.activeCount();

        cpuRatio = 0;
        if (osName.toLowerCase().startsWith("windows")) {
            cpuRatio = this.getCpuRatioForWindows();
        }
    }

    public StatusManager getManager() {
        init();
        return this;
    }

    /**
     * 获得CPU使用率.
     *
     * @return 返回cpu使用率
     */
    private double getCpuRatioForWindows() {
        try {
            String procCmd = System.getenv("windir")
                    + "//system32//wbem//wmic.exe process get Caption,CommandLine,"
                    + "KernelModeTime,ReadOperationCount,ThreadCount,UserModeTime,WriteOperationCount";
            // 取进程信息
            long[] c0 = readCpu(Runtime.getRuntime().exec(procCmd));
            Thread.sleep(CPUTIME);
            long[] c1 = readCpu(Runtime.getRuntime().exec(procCmd));
            if (c0 != null && c1 != null) {
                long idletime = c1[0] - c0[0];
                long busytime = c1[1] - c0[1];
                return (double) (PERCENT * (busytime) / (busytime + idletime));
            } else {
                return 0.0;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0.0;
        }
    }

    /**
     * 读取CPU信息.
     *
     * @param proc process
     * @return cpu
     */
    private long[] readCpu(final Process proc) {
        long[] retn = new long[2];
        try {
            proc.getOutputStream().close();
            InputStreamReader ir = new InputStreamReader(proc.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            String line = input.readLine();
            if (line == null || line.length() < FAULTLENGTH) {
                return null;
            }
            int capidx = line.indexOf("Caption");
            int cmdidx = line.indexOf("CommandLine");
            int rocidx = line.indexOf("ReadOperationCount");
            int umtidx = line.indexOf("UserModeTime");
            int kmtidx = line.indexOf("KernelModeTime");
            int wocidx = line.indexOf("WriteOperationCount");
            long idletime = 0;
            long kneltime = 0;
            long usertime = 0;
            while ((line = input.readLine()) != null) {
                if (line.length() < wocidx) {
                    continue;
                }
                // 字段出现顺序：Caption,CommandLine,KernelModeTime,ReadOperationCount,
                // ThreadCount,UserModeTime,WriteOperation
                String caption = Bytes.substring(line, capidx, cmdidx - 1)
                        .trim();
                String cmd = Bytes.substring(line, cmdidx, kmtidx - 1).trim();
                if (cmd.contains("wmic.exe")) {
                    continue;
                }
                // log.info("line="+line);
                if (caption.equals("System Idle Process")
                        || caption.equals("System")) {
                    idletime += Long.valueOf(
                            Bytes.substring(line, kmtidx, rocidx - 1).trim());
                    idletime += Long.valueOf(
                            Bytes.substring(line, umtidx, wocidx - 1).trim());
                    continue;
                }

                kneltime += Long.valueOf(
                        Bytes.substring(line, kmtidx, rocidx - 1).trim());
                usertime += Long.valueOf(
                        Bytes.substring(line, umtidx, wocidx - 1).trim());
            }
            retn[0] = idletime;
            retn[1] = kneltime + usertime;
            return retn;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                proc.getInputStream().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // TEST
    public static void main(String[] args) {
        StatusManager statusManager = new StatusManager();
        System.out.println("CPU占有率\t" + statusManager.getCpuRatio());
        System.out.println("可使用内存\t" + statusManager.getTotalMemory());
        System.out.println("剩余内存\t\t" + statusManager.getFreeMemory());
        System.out.println("最大内存\t\t" + statusManager.getMaxMemory());
        System.out.println("操作系统\t\t" + statusManager.getOsName());
        System.out.println("线程总数\t\t" + statusManager.getTotalThread());
    }

}

