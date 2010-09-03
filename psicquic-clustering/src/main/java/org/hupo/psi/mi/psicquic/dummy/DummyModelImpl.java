package org.hupo.psi.mi.psicquic.dummy;

public class DummyModelImpl implements DummyModel {

    public void processSubmission(Object obj) {
        System.out.printf("processing: %s record in thread: %s\n", obj, Thread.currentThread().toString());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
