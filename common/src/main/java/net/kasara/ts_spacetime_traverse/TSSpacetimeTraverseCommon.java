package net.kasara.ts_spacetime_traverse;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/**
 * ローダー非依存の共通定数。各ローダーのTSSpacetimeTraverse.MOD_ID/LOGGERはこれを参照する。
 * (TSMultiToolsCommonと同じパターン)
 */
public final class TSSpacetimeTraverseCommon {

    public static final String MOD_ID = "ts_spacetime_traverse";
    public static final Logger LOGGER = LogUtils.getLogger();

    private TSSpacetimeTraverseCommon() {}
}
