package br.unb.cic.tdp.util;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Triplet<F, S, T> {

    public F first;
    public S second;
    public T third;

    @Override
    public String toString() {
        return "Triplet{" +
                "first=" + first +
                ", second=" + second +
                ", third=" + third +
                '}';
    }
}
