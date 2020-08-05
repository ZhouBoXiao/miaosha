package miaosha.config;


import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * mdc
 **/
@Slf4j
public class MdcThreadPoolExecutor extends ThreadPoolExecutor {
    private static final List<MdcThreadPoolExecutor> EXECUTOR_LIST = new ArrayList<>();
    /**
     * Monitor Thread pool periodically.
     */
    private final static ScheduledExecutorService scheduledExecutorService =
            new ScheduledThreadPoolExecutor(1, new ThreadFactoryBuilder().setNameFormat("MDC Thread Monitor")
                    .setDaemon(true).build());
    @Getter
    final private String name;
    final private boolean useFixedContext;
    final private Map<String, String> fixedContext;

    private MdcThreadPoolExecutor(String name, Map<String, String> fixedContext, int corePoolSize, int maximumPoolSize,
                                  long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        this.fixedContext = fixedContext;
        useFixedContext = (fixedContext != null);
        this.name = name;
        synchronized (EXECUTOR_LIST) {
            EXECUTOR_LIST.add(this);
        }
    }

    private MdcThreadPoolExecutor(String name, Map<String, String> fixedContext, int corePoolSize, int maximumPoolSize,
                                  long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        this.fixedContext = fixedContext;
        useFixedContext = (fixedContext != null);
        this.name = name;
        synchronized (EXECUTOR_LIST) {
            EXECUTOR_LIST.add(this);
        }
    }


    private MdcThreadPoolExecutor(String name, Map<String, String> fixedContext, int corePoolSize, int maximumPoolSize,
                                  long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                  ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        this.fixedContext = fixedContext;
        useFixedContext = (fixedContext != null);
        this.name = name;
        synchronized (EXECUTOR_LIST) {
            EXECUTOR_LIST.add(this);
        }
    }

    private static void monitor(MdcThreadPoolExecutor pool) {
        if (!pool.isTerminated()) {
            log.info("MDC Thread Pool[{}]: core:{}, maximum:{}, largest:{}, poolSize:{}, active:{}, queue:{}",
                     pool.getName(), pool.getCorePoolSize(), pool.getMaximumPoolSize(), pool.getLargestPoolSize(),
                     pool.getPoolSize(), pool.getActiveCount(), pool.getQueue().size());
        }
    }

    /**
     * Pool where task threads take MDC from the submitting thread.
     */
    public static MdcThreadPoolExecutor newWithInheritedMdc(String name, int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                                            TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        return new MdcThreadPoolExecutor(name, null, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    /**
     * Pool where task threads take MDC from the submitting thread.
     */
    public static MdcThreadPoolExecutor newWithInheritedMdc(String name, int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                                            TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                                            ThreadFactory threadFactory) {
        return new MdcThreadPoolExecutor(name, null, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    /**
     * Pool where task threads take MDC from the submitting thread.
     */
    public static MdcThreadPoolExecutor newWithInheritedMdc(String name, int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                                            TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                                            ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        return new MdcThreadPoolExecutor(name, null, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    /**
     * Pool where task threads take fixed MDC from the thread that creates the pool.
     */
    public static MdcThreadPoolExecutor newWithCurrentMdc(String name, int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                                          TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                                          ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        return new MdcThreadPoolExecutor(name, MDC.getCopyOfContextMap(), corePoolSize, maximumPoolSize, keepAliveTime, unit,
                                         workQueue, threadFactory, handler);
    }

    /**
     * Pool where task threads take fixed MDC from the thread that creates the pool.
     */
    public static MdcThreadPoolExecutor newWithCurrentMdc(String name, int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                                          TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        return new MdcThreadPoolExecutor(name, MDC.getCopyOfContextMap(), corePoolSize, maximumPoolSize, keepAliveTime, unit,
                                         workQueue);
    }

    /**
     * Pool where task threads always have a specified, fixed MDC.
     */
    public static MdcThreadPoolExecutor newWithFixedMdc(String name, Map<String, String> fixedContext, int corePoolSize,
                                                        int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                                        BlockingQueue<Runnable> workQueue) {
        return new MdcThreadPoolExecutor(name, fixedContext, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    private static Runnable wrap(final Runnable runnable, final Map<String, String> context) {
        return () -> {
            Map<String, String> previous = MDC.getCopyOfContextMap();
            if (context == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(context);
            }
            try {
                runnable.run();
            } finally {
                if (previous == null) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(previous);
                }
            }
        };
    }

    @PostConstruct
    public void init() {
        scheduledExecutorService.scheduleAtFixedRate(() -> EXECUTOR_LIST.forEach(MdcThreadPoolExecutor::monitor),
                                                     0, 1, TimeUnit.MINUTES);
    }

    @Override
    protected void terminated() {
        super.terminated();
        log.info("MDC Thread Pool[{}]: TERMINATED");
        synchronized (EXECUTOR_LIST) {
            EXECUTOR_LIST.remove(this);
        }
    }

    private Map<String, String> getContextForTask() {
        return useFixedContext ? fixedContext : MDC.getCopyOfContextMap();
    }

    /**
     * All executions will have MDC injected. {@code ThreadPoolExecutor}'s submission methods ({@code submit()} etc.)
     * all delegate to this.
     */
    @Override
    public void execute(Runnable command) {
        super.execute(wrap(command, getContextForTask()));
    }
}
