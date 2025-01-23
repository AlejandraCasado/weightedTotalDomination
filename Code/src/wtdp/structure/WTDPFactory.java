package wtdp.structure;

import grafo.optilib.structure.InstanceFactory;

public class WTDPFactory extends InstanceFactory<WTDPInstance> {
    @Override
    public WTDPInstance readInstance(String s) {
        return new WTDPInstance(s);
    }
}
