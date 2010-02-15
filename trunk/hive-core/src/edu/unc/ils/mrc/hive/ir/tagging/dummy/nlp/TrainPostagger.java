package edu.unc.ils.mrc.hive.ir.tagging.dummy.nlp;
import com.aliasi.io.FileExtensionFilter;

import com.aliasi.corpus.Parser;
import com.aliasi.corpus.TagHandler;

import com.aliasi.corpus.parsers.MedPostPosParser;

import com.aliasi.hmm.HmmCharLmEstimator;

import com.aliasi.util.Files;
import com.aliasi.util.Streams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class TrainPostagger {

    // language model parameters for HMM emissions
    static int N_GRAM = 8;
    static int NUM_CHARS = 256;
    static double LAMBDA_FACTOR = 8.0;

    public static void main(String[] args) throws IOException {

        // set up parser with estimator as handler
        HmmCharLmEstimator estimator
            = new HmmCharLmEstimator(N_GRAM,NUM_CHARS,LAMBDA_FACTOR);
        Parser<TagHandler> parser = new MedPostPosParser();
        parser.setHandler(estimator);

        // train on files in data directory ending in "ioc"
        File dataDir = new File("/home/jose/workspace/SNIP-IT/data/medtag/medpost/");
        File[] files = dataDir.listFiles(new FileExtensionFilter("ioc"));
        for (int i = 0; i < files.length; ++i) {
            System.out.println("Training file=" + files[i]);
            parser.parse(files[i]);
        }

        // write output to file
        File modelFile = new File("/home/jose/workspace/SNIP-IT/data/models/medtagModel");
        FileOutputStream fileOut = new FileOutputStream(modelFile);
        ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
        estimator.compileTo(objOut);
        Streams.closeOutputStream(objOut);
    }

}

