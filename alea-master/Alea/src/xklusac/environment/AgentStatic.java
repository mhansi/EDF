/*
 Copyright (c) 2014-2015 Simon Toth (kontakt@simontoth.cz)

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in
 the Software without restriction, including without limitation the rights to
 use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 of the Software, and to permit persons to whom the Software is furnished to do
 so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package xklusac.environment;

import alea.core.WorkloadReaderSWF;
import alea.dynamic.JobBatchState;
import alea.dynamic.JobBatchStatic;
import java.io.BufferedReader;
import java.util.Random;
import xklusac.extensions.*;

/**
 * Class AgentStatic<p>
 * Loads jobs dynamically over time from a static workload trace. Then sends
 * these gridlets to the scheduler. Workload strace is expected in SWF. SWF
 * stands for Standard Workloads Format (SWF).
 *
 * @author Simon Toth (kontakt@simontoth.cz)
 */
public class AgentStatic extends AgentSkeleton {

    /* Next job to be submitted into the simulator */
    private ComplexGridlet next_job = null;
    private JobBatchStatic static_batch = null;

    /**
     * Creates a new instance of JobLoader
     */
    public AgentStatic(String name, String loader_name, double baudRate, String data_set, int maxPE, int minPErating, int maxPErating) throws Exception {
        super(name, loader_name, baudRate);

        static_batch = new JobBatchStatic(name, data_set, name, "batch001", maxPE, minPErating, maxPErating);
        next_job = static_batch.getNextJob();
    }

    /** When a job submission is confirmed, read the next job */
    @Override
    boolean onJobEnqueued(ComplexGridlet job) {
        next_job = static_batch.getNextJob();
        return true;
    }

    /** No special reaction to job start */
    @Override
    boolean onJobStarted(ComplexGridlet job) {
        return false;
    }

    /** If a job is aborted, notify the static batch */
    @Override
    boolean onJobAborted(ComplexGridlet job) {
        static_batch.notifyJobFail(job);
        return false;
    }

    /** If a job finishes running, notify the static batch */
    @Override
    boolean onJobCompleted(ComplexGridlet job) {
        static_batch.notifyJobCompletion(job);
        return false;
    }

    /** Return the current job */
    @Override
    ComplexGridlet getCurrentJob() {
        if (static_batch.getCurrentState() != JobBatchState.DONE_SUBMITTING && static_batch.getCurrentState() != JobBatchState.FINISHED) {
            return next_job;
        } else {
            return null;
        }
    }

    /** Static agent inherits state from the parent batch */
    @Override
    JobBatchState getAgentState() {
        return static_batch.getCurrentState();
    }
}
