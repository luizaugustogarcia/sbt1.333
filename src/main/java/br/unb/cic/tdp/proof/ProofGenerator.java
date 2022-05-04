package br.unb.cic.tdp.proof;

import br.unb.cic.tdp.base.Configuration;
import br.unb.cic.tdp.permutation.Cycle;
import br.unb.cic.tdp.permutation.MulticyclePermutation;
import br.unb.cic.tdp.proof.seq16_12.Combinations;
import br.unb.cic.tdp.proof.seq16_12.Extensions;
import br.unb.cic.tdp.proof.util.*;
import cern.colt.list.IntArrayList;
import com.google.common.base.Throwables;
import com.google.common.primitives.Ints;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.eclipse.collections.api.set.primitive.MutableIntSet;

import java.io.Writer;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static br.unb.cic.tdp.permutation.PermutationGroups.computeProduct;
import static br.unb.cic.tdp.util._19_14Seqs._19_14;
import static br.unb.cic.tdp.util._20_15Seqs._20_15;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class ProofGenerator {

    static final int[][] _4_3 = new int[][]{{0, 2, 2, 2}};

    static final int[][] _8_6 = new int[][]{
            {0, 2, 2, 0, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 2, 2, 2}};

    static final int[][] _12_9 = new int[][]{
            {0, 2, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2}
    };

    static final int[][] _15_11 = new int[][]{
            {0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 0, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 0, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 0, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 0, 0, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2},
            {0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2},
            {0, 0, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 0, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 0, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 0, 2, 0, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2},
            {0, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2},
            {0, 0, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 0, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 0, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2},
            {0, 0, 2, 2, 0, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2},
            {0, 0, 2, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 0, 2, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 0, 2, 2, 2, 2, 2, 0, 2, 2, 2},
            {0, 0, 2, 2, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 2, 0, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 2, 0, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2, 0, 2, 2, 2},
            {0, 0, 2, 2, 2, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 2, 2, 0, 2, 0, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 2, 2, 0, 2, 2, 0, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 2, 2, 0, 2, 2, 2, 0, 2, 2, 2},
            {0, 2, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 0, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 0, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 0, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 2, 0, 0, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2},
            {0, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2},
            {0, 2, 0, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 0, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 0, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2},
            {0, 2, 0, 2, 0, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2},
            {0, 2, 0, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 0, 2, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 0, 2, 2, 2, 2, 2, 0, 2, 2, 2},
            {0, 2, 0, 2, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 2, 0, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 2, 0, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2, 0, 2, 2, 2},
            {0, 2, 0, 2, 2, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 2, 2, 0, 2, 0, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 2, 2, 0, 2, 2, 0, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 2, 2, 0, 2, 2, 2, 0, 2, 2, 2},
            {0, 2, 2, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 0, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 0, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 0, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2},
            {0, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2},
            {0, 2, 2, 0, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 0, 2, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 0, 2, 2, 2, 2, 2, 0, 2, 2, 2},
            {0, 2, 2, 0, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 2, 0, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 2, 0, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 2, 0, 2, 2, 2, 2, 0, 2, 2, 2},
            {0, 2, 2, 0, 2, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 2, 2, 0, 2, 0, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 2, 2, 0, 2, 2, 0, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 2, 2, 0, 2, 2, 2, 0, 2, 2, 2}
    };

    static final int[][] _16_12 = new int[][]{
            {0, 2, 2, 0, 2, 2, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 2, 2, 0, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 2, 2, 0, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 2, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 2, 0, 2, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 2, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 0, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 0, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2},
            {0, 2, 2, 0, 0, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 0, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 0, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 2, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 2, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 2, 2, 0, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 2, 2, 0, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 2, 0, 2, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 2, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 0, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 0, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 0, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2},
            {0, 2, 0, 2, 0, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 0, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2},
            {0, 2, 0, 0, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 2, 0, 0, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 0, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 0, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 2, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 2, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 2, 2, 0, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 2, 2, 0, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 2, 0, 2, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 2, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 0, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 0, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 0, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2},
            {0, 0, 2, 2, 0, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 0, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2},
            {0, 0, 2, 0, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 0, 2, 0, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 0, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2},
            {0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2},
            {0, 0, 0, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2},
            {0, 0, 0, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 0, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 0, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 0, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2}};

    public static final Move _4_3_SEQS = new Move(0, new Move[0], null);
    public static final Move _8_6_SEQS = new Move(0, new Move[0], null);
    public static final Move _12_9_SEQS = new Move(0, new Move[0], null);
    public static final Move _15_11_SEQS = new Move(0, new Move[0], null);
    public static final Move _16_12_SEQS = new Move(0, new Move[0], null);
    public static final Move _20_15_SEQS = new Move(0, new Move[0], null);
    public static final Move _19_14_SEQS = new Move(0, new Move[0], null);

    static {
        try {
            toTrie(_4_3, _4_3_SEQS);
            toTrie(_8_6, _8_6_SEQS);
            toTrie(_12_9, _12_9_SEQS);
            toTrie(_15_11, _15_11_SEQS);
            toTrie(_16_12, _16_12_SEQS);
            toTrie(_19_14, _19_14_SEQS);
            toTrie(_20_15, _20_15_SEQS);
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    public static void toTrie(final int[][] seqs, Move root) {
        final var root_ = root;
        for (int[] seq : seqs) {
            root = root_;
            for (int j = 1; j < seq.length; j++) {
                final var move = seq[j];
                if (Arrays.stream(root.children).noneMatch(m -> m.mu == move)) {
                    if (root.children.length == 0) {
                        root.children = new Move[1];
                        root.children[0] = new Move(move, new Move[0], root);
                    } else {
                        final var children = new Move[2];
                        children[0] = root.children[0];
                        children[1] = new Move(move, new Move[0], root);
                        root.children = children;
                    }
                }
                root = Arrays.stream(root.children).filter(m -> m.mu == move).findFirst().get();
            }
        }
    }

    // mvn exec:java -Dexec.mainClass="br.unb.cic.tdp.proof.ProofGenerator" -Dexec.args=".\\proof\\"
    public static void main(String[] args) throws Throwable {
        Velocity.setProperty("resource.loader", "class");
        Velocity.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.setProperty("parser.pool.size", 100);
        Velocity.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogChute");
        Velocity.init();

        Files.createDirectories(Paths.get(args[0]));

        Files.copy(ProofGenerator.class.getClassLoader().getResourceAsStream("index.html"),
                Paths.get(args[0] + "/index.html"), REPLACE_EXISTING);
        Files.copy(ProofGenerator.class.getClassLoader().getResourceAsStream("explain.html"),
                Paths.get(args[0] + "/explain.html"), REPLACE_EXISTING);
        Files.copy(ProofGenerator.class.getClassLoader().getResourceAsStream("draw-config.js"),
                Paths.get(args[0] + "/draw-config.js"), REPLACE_EXISTING);

        Extensions.generate(args[0]);
        Combinations.generate(args[0]);
    }

    public static Cycle removeExtraSymbols(final MutableIntSet symbols, final Cycle pi) {
        final var newPi = new IntArrayList(symbols.size());
        for (final var symbol : pi.getSymbols()) {
            if (symbols.contains(symbol))
                newPi.add(symbol);
        }
        return Cycle.create(newPi);
    }

    public static String permutationToJsArray(final MulticyclePermutation permutation) {
        return "[" + permutation
                .stream().map(c -> "[" + Ints.asList(c.getSymbols()).stream()
                        .map(s -> Integer.toString(s))
                        .collect(Collectors.joining(",")) + "]")
                .collect(Collectors.joining(",")) + "]";
    }

    private static String cycleToJsArray(final Cycle cycle) {
        return "[" + Ints.asList(cycle.getSymbols()).stream()
                .map(s -> Integer.toString(s))
                .collect(Collectors.joining(",")) + "]";
    }

    public static void renderSorting(final Configuration canonicalConfig, final List<Cycle> sorting, final Writer writer) {
        VelocityContext context = new VelocityContext();

        context.put("spi", canonicalConfig.getSpi());
        context.put("piSize", canonicalConfig.getPi().size());
        context.put("jsSpi", permutationToJsArray(canonicalConfig.getSpi()));
        context.put("jsPi", cycleToJsArray(canonicalConfig.getPi()));
        context.put("sorting", sorting);

        final var spis = new ArrayList<MulticyclePermutation>();
        final var jsSpis = new ArrayList<String>();
        final var jsPis = new ArrayList<String>();
        var spi = canonicalConfig.getSpi();
        var pi = canonicalConfig.getPi();
        for (final Cycle move : sorting) {
            spis.add(spi = computeProduct(spi, move.getInverse()));
            jsSpis.add(permutationToJsArray(spi));
            jsPis.add(cycleToJsArray(pi = computeProduct(move, pi).asNCycle()));
        }
        context.put("spis", spis);
        context.put("jsSpis", jsSpis);
        context.put("jsPis", jsPis);

        final var template = Velocity.getTemplate("templates/sorting.html");
        template.merge(context, writer);
    }
}