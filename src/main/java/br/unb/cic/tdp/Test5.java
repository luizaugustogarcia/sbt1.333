package br.unb.cic.tdp;

import br.unb.cic.tdp.base.CommonOperations;
import br.unb.cic.tdp.base.Configuration;

public class Test5 {
    public static void main(String[] args) {
        final var c = new Configuration("(0 34 20)(1 35 21)(2 19 17 15 4)(3 7 5)(6)(8 12 10)(9 26 24 13 11)(14 18 16)(22 32 30)(23 33 31)(25 29 27)(28)");
        System.out.println(CommonOperations.getComponents(c.getSpi(), c.getPi()));
    }
}
