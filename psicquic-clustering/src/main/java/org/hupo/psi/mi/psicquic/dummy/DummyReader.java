package org.hupo.psi.mi.psicquic.dummy;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

public class DummyReader implements ItemReader<String> {

    private int count = 0;

    public synchronized String read() throws Exception, UnexpectedInputException,
            ParseException {

        if(count < 100) {
            count++;
            return "test " + count;
        }

        return null;
    }

}

