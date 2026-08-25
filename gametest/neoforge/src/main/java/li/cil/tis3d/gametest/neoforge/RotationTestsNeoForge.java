/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest.neoforge;

import li.cil.tis3d.gametest.RotationTests;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.GameTest;

import static li.cil.tis3d.gametest.TestSupport.TEMPLATE_ID;

@ForEachTest(groups = "rotation")
public final class RotationTestsNeoForge {
    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Faces round trip through world space.")
    public static void facesRoundTripThroughWorldSpace(final GameTestHelper helper) {
        RotationTests.facesRoundTripThroughWorldSpace(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Ports round trip through world space.")
    public static void portsRoundTripThroughWorldSpace(final GameTestHelper helper) {
        RotationTests.portsRoundTripThroughWorldSpace(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Edge topology is rotation coherent.")
    public static void edgeTopologyIsRotationCoherent(final GameTestHelper helper) {
        RotationTests.edgeTopologyIsRotationCoherent(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Rotating block state moves face flags.")
    public static void rotatingBlockStateMovesFaceFlags(final GameTestHelper helper) {
        RotationTests.rotatingBlockStateMovesFaceFlags(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Value crosses faces rotated none.")
    public static void valueCrossesFacesRotatedNone(final GameTestHelper helper) {
        RotationTests.valueCrossesFacesWhenRotated(helper, Rotation.NONE);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Value crosses faces rotated cw90.")
    public static void valueCrossesFacesRotatedCw90(final GameTestHelper helper) {
        RotationTests.valueCrossesFacesWhenRotated(helper, Rotation.CLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Value crosses faces rotated cw180.")
    public static void valueCrossesFacesRotatedCw180(final GameTestHelper helper) {
        RotationTests.valueCrossesFacesWhenRotated(helper, Rotation.CLOCKWISE_180);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Value crosses faces rotated ccw90.")
    public static void valueCrossesFacesRotatedCcw90(final GameTestHelper helper) {
        RotationTests.valueCrossesFacesWhenRotated(helper, Rotation.COUNTERCLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Value crosses casings rotated none cw90.")
    public static void valueCrossesCasingsRotatedNoneCw90(final GameTestHelper helper) {
        RotationTests.valueCrossesCasingsWhenRotated(helper, Rotation.NONE, Rotation.CLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Value crosses casings rotated cw90 none.")
    public static void valueCrossesCasingsRotatedCw90None(final GameTestHelper helper) {
        RotationTests.valueCrossesCasingsWhenRotated(helper, Rotation.CLOCKWISE_90, Rotation.NONE);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Value crosses casings rotated cw90 cw180.")
    public static void valueCrossesCasingsRotatedCw90Cw180(final GameTestHelper helper) {
        RotationTests.valueCrossesCasingsWhenRotated(helper, Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_180);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Value crosses casings rotated ccw90 ccw90.")
    public static void valueCrossesCasingsRotatedCcw90Ccw90(final GameTestHelper helper) {
        RotationTests.valueCrossesCasingsWhenRotated(helper, Rotation.COUNTERCLOCKWISE_90, Rotation.COUNTERCLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Value crosses stacked casings rotated none none.")
    public static void valueCrossesStackedCasingsRotatedNoneNone(final GameTestHelper helper) {
        RotationTests.valueCrossesStackedCasingsWhenRotated(helper, Rotation.NONE, Rotation.NONE);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Value crosses stacked casings rotated cw90 none.")
    public static void valueCrossesStackedCasingsRotatedCw90None(final GameTestHelper helper) {
        RotationTests.valueCrossesStackedCasingsWhenRotated(helper, Rotation.CLOCKWISE_90, Rotation.NONE);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Value crosses stacked casings rotated none cw90.")
    public static void valueCrossesStackedCasingsRotatedNoneCw90(final GameTestHelper helper) {
        RotationTests.valueCrossesStackedCasingsWhenRotated(helper, Rotation.NONE, Rotation.CLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Value crosses stacked casings rotated cw90 cw180.")
    public static void valueCrossesStackedCasingsRotatedCw90Cw180(final GameTestHelper helper) {
        RotationTests.valueCrossesStackedCasingsWhenRotated(helper, Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_180);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Rotating module against neighbor drops it.")
    public static void rotatingModuleAgainstNeighborDropsIt(final GameTestHelper helper) {
        RotationTests.rotatingModuleAgainstNeighborDropsIt(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Execution module ports follow world facing on cap face none.")
    public static void executionModulePortsFollowWorldFacingOnCapFaceNone(final GameTestHelper helper) {
        RotationTests.executionModulePortsFollowWorldFacingOnCapFace(helper, Rotation.NONE);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Execution module ports follow world facing on cap face cw90.")
    public static void executionModulePortsFollowWorldFacingOnCapFaceCw90(final GameTestHelper helper) {
        RotationTests.executionModulePortsFollowWorldFacingOnCapFace(helper, Rotation.CLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Execution module ports follow world facing on cap face cw180.")
    public static void executionModulePortsFollowWorldFacingOnCapFaceCw180(final GameTestHelper helper) {
        RotationTests.executionModulePortsFollowWorldFacingOnCapFace(helper, Rotation.CLOCKWISE_180);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Execution module ports follow world facing on cap face ccw90.")
    public static void executionModulePortsFollowWorldFacingOnCapFaceCcw90(final GameTestHelper helper) {
        RotationTests.executionModulePortsFollowWorldFacingOnCapFace(helper, Rotation.COUNTERCLOCKWISE_90);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 200)
    @TestHolder(description = "Setting facing directly rederives face flags.")
    public static void settingFacingDirectlyRederivesFaceFlags(final GameTestHelper helper) {
        RotationTests.settingFacingDirectlyRederivesFaceFlags(helper);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 300)
    @TestHolder(description = "Hoppers reach the casing through world faces none.")
    public static void hoppersReachTheCasingThroughWorldFacesNone(final GameTestHelper helper) {
        RotationTests.hoppersReachTheCasingThroughWorldFaces(helper, Rotation.NONE);
    }

    @GameTest(template = TEMPLATE_ID, timeoutTicks = 300)
    @TestHolder(description = "Hoppers reach the casing through world faces cw90.")
    public static void hoppersReachTheCasingThroughWorldFacesCw90(final GameTestHelper helper) {
        RotationTests.hoppersReachTheCasingThroughWorldFaces(helper, Rotation.CLOCKWISE_90);
    }

    // --------------------------------------------------------------------- //

    private RotationTestsNeoForge() {
    }
}
