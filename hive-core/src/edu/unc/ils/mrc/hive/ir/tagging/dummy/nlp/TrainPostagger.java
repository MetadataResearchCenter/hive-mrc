/**
 * Copyright (c) 2010, UNC-Chapel Hill and Nescent
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided 
that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this list of conditions and 
 * the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the UNC-Chapel Hill or Nescent nor the names of its contributors may be used to endorse or promote 
 * products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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

