/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest.neoforge;

import li.cil.tis3d.gametest.RotationTests;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static li.cil.tis3d.gametest.TestSupport.MOD_ID;
import static li.cil.tis3d.gametest.TestSupport.TEMPLATE;

@GameTestHolder(MOD_ID)
@PrefixGameTestTemplate(false)
public final class RotationTestsNeoForge {
    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void facesRoundTripThroughWorldSpace(final GameTestHelper helper) {
        RotationTests.facesRoundTripThroughWorldSpace(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void portsRoundTripThroughWorldSpace(final GameTestHelper helper) {
        RotationTests.portsRoundTripThroughWorldSpace(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void edgeTopologyIsRotationCoherent(final GameTestHelper helper) {
        RotationTests.edgeTopologyIsRotationCoherent(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void rotatingBlockStateMovesFaceFlags(final GameTestHelper helper) {
        RotationTests.rotatingBlockStateMovesFaceFlags(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void valueCrossesFacesRotatedNone(final GameTestHelper helper) {
        RotationTests.valueCrossesFacesWhenRotated(helper, Rotation.NONE);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void valueCrossesFacesRotatedCw90(final GameTestHelper helper) {
        RotationTests.valueCrossesFacesWhenRotated(helper, Rotation.CLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void valueCrossesFacesRotatedCw180(final GameTestHelper helper) {
        RotationTests.valueCrossesFacesWhenRotated(helper, Rotation.CLOCKWISE_180);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void valueCrossesFacesRotatedCcw90(final GameTestHelper helper) {
        RotationTests.valueCrossesFacesWhenRotated(helper, Rotation.COUNTERCLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void valueCrossesCasingsRotatedNoneCw90(final GameTestHelper helper) {
        RotationTests.valueCrossesCasingsWhenRotated(helper, Rotation.NONE, Rotation.CLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void valueCrossesCasingsRotatedCw90None(final GameTestHelper helper) {
        RotationTests.valueCrossesCasingsWhenRotated(helper, Rotation.CLOCKWISE_90, Rotation.NONE);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void valueCrossesCasingsRotatedCw90Cw180(final GameTestHelper helper) {
        RotationTests.valueCrossesCasingsWhenRotated(helper, Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_180);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void valueCrossesCasingsRotatedCcw90Ccw90(final GameTestHelper helper) {
        RotationTests.valueCrossesCasingsWhenRotated(helper, Rotation.COUNTERCLOCKWISE_90, Rotation.COUNTERCLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void valueCrossesStackedCasingsRotatedNoneNone(final GameTestHelper helper) {
        RotationTests.valueCrossesStackedCasingsWhenRotated(helper, Rotation.NONE, Rotation.NONE);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void valueCrossesStackedCasingsRotatedCw90None(final GameTestHelper helper) {
        RotationTests.valueCrossesStackedCasingsWhenRotated(helper, Rotation.CLOCKWISE_90, Rotation.NONE);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void valueCrossesStackedCasingsRotatedNoneCw90(final GameTestHelper helper) {
        RotationTests.valueCrossesStackedCasingsWhenRotated(helper, Rotation.NONE, Rotation.CLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void valueCrossesStackedCasingsRotatedCw90Cw180(final GameTestHelper helper) {
        RotationTests.valueCrossesStackedCasingsWhenRotated(helper, Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_180);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void rotatingModuleAgainstNeighborDropsIt(final GameTestHelper helper) {
        RotationTests.rotatingModuleAgainstNeighborDropsIt(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void executionModulePortsFollowLocalFacingOnCapFaceNone(final GameTestHelper helper) {
        RotationTests.executionModulePortsFollowLocalFacingOnCapFace(helper, Rotation.NONE);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void executionModulePortsFollowLocalFacingOnCapFaceCw90(final GameTestHelper helper) {
        RotationTests.executionModulePortsFollowLocalFacingOnCapFace(helper, Rotation.CLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void executionModulePortsFollowLocalFacingOnCapFaceCw180(final GameTestHelper helper) {
        RotationTests.executionModulePortsFollowLocalFacingOnCapFace(helper, Rotation.CLOCKWISE_180);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void executionModulePortsFollowLocalFacingOnCapFaceCcw90(final GameTestHelper helper) {
        RotationTests.executionModulePortsFollowLocalFacingOnCapFace(helper, Rotation.COUNTERCLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void settingFacingDirectlyRederivesFaceFlags(final GameTestHelper helper) {
        RotationTests.settingFacingDirectlyRederivesFaceFlags(helper);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 300)
    public static void hoppersReachTheCasingThroughWorldFacesNone(final GameTestHelper helper) {
        RotationTests.hoppersReachTheCasingThroughWorldFaces(helper, Rotation.NONE);
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 300)
    public static void hoppersReachTheCasingThroughWorldFacesCw90(final GameTestHelper helper) {
        RotationTests.hoppersReachTheCasingThroughWorldFaces(helper, Rotation.CLOCKWISE_90);
    }

    // --------------------------------------------------------------------- //

    private RotationTestsNeoForge() {
    }
}
