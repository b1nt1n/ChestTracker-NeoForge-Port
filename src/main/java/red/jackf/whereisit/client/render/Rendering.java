package red.jackf.whereisit.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.SearchResult;

import java.util.*;

public final class Rendering {
    private static final Map<BlockPos, SearchResult> RESULTS = new HashMap<>();
    private static final Map<BlockPos, SearchResult> NAMED_RESULTS = new HashMap<>();
    private static final List<ScheduledLabel> SCHEDULED_LABELS = new ArrayList<>();
    private static final int FADE_TICKS = 100;
    private static long ticksSinceSearch = FADE_TICKS + 1;
    private static SearchRequest lastRequest = null;

    private record ScheduledLabel(Vec3 position, Component text, boolean seeThrough) {
    }

    private Rendering() {
    }

    public static void setup() {
        NeoForge.EVENT_BUS.register(EventHandler.class);
    }

    public static void addResults(Collection<SearchResult> newResults) {
        for (SearchResult result : newResults) {
            RESULTS.put(result.pos(), result);
            if (result.name() != null) NAMED_RESULTS.put(result.pos(), result);
        }
    }

    public static void setLastRequest(SearchRequest request) {
        lastRequest = request;
    }

    public static void clearResults() {
        RESULTS.clear();
        NAMED_RESULTS.clear();
        SCHEDULED_LABELS.clear();
        lastRequest = null;
    }

    public static void resetSearchTime() {
        ticksSinceSearch = 0;
    }

    public static Map<BlockPos, SearchResult> getResults() {
        return Collections.unmodifiableMap(RESULTS);
    }

    public static Map<BlockPos, SearchResult> getNamedResults() {
        return Collections.unmodifiableMap(NAMED_RESULTS);
    }

    public static void scheduleLabel(Vec3 pos, Component name, boolean seeThrough) {
        if (pos != null && name != null) {
            SCHEDULED_LABELS.add(new ScheduledLabel(pos, name, seeThrough));
        }
    }

    private static boolean shouldBeRendering() {
        return !RESULTS.isEmpty() && ticksSinceSearch <= FADE_TICKS;
    }

    private static float progress(float partialTick) {
        return Mth.clamp((ticksSinceSearch + partialTick) / (float) FADE_TICKS, 0f, 1f);
    }

    private static final class EventHandler {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Pre event) {
            if (!RESULTS.isEmpty() && ++ticksSinceSearch > FADE_TICKS) {
                clearResults();
            }
        }

        @SubscribeEvent
        public static void onRenderLevel(RenderLevelStageEvent event) {
            if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
            if (shouldBeRendering()) {
                renderBoxes(event.getPoseStack(), event.getCamera(), progress(0));
            }
            if (!SCHEDULED_LABELS.isEmpty() || shouldBeRendering()) {
                renderLabels(event.getPoseStack(), event.getCamera());
                SCHEDULED_LABELS.clear();
            }
        }

        @SubscribeEvent
        public static void onScreenRender(ScreenEvent.Render.Post event) {
            if (!shouldBeRendering() || lastRequest == null) return;
            renderSlotHighlights(event.getScreen(), event.getGuiGraphics());
        }
    }

    private static void renderSlotHighlights(Screen screen, GuiGraphics graphics) {
        if (!(screen instanceof AbstractContainerScreen<?> container)) return;
        int overlayColor = 0x66FFFF00;
        for (Slot slot : container.getMenu().slots) {
            if (!slot.isActive() || !slot.hasItem() || !SearchRequest.check(slot.getItem(), lastRequest)) continue;
            int x = container.getGuiLeft() + slot.x;
            int y = container.getGuiTop() + slot.y;
            graphics.fill(x, y, x + 16, y + 16, overlayColor);
        }
    }

    private static void renderBoxes(PoseStack poseStack, Camera camera, float progress) {
        float alpha = 1.0f - progress * 0.5f;
        float scale = 0.5f + (1.0f - progress) * 0.15f;
        Vec3 camPos = camera.getPosition();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        for (SearchResult result : RESULTS.values()) {
            renderBoxOutline(buffer, poseStack, result.pos(), camPos, alpha, scale);
            for (BlockPos other : result.otherPositions()) {
                renderBoxOutline(buffer, poseStack, other, camPos, alpha, scale);
            }
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static void renderBoxOutline(VertexConsumer buffer, PoseStack poseStack, BlockPos pos, Vec3 camPos, float alpha, float halfSize) {
        double x = pos.getX() + 0.5 - camPos.x;
        double y = pos.getY() + 0.5 - camPos.y;
        double z = pos.getZ() + 0.5 - camPos.z;
        Matrix4f matrix = poseStack.last().pose();
        float minX = (float) (x - halfSize);
        float minY = (float) (y - halfSize);
        float minZ = (float) (z - halfSize);
        float maxX = (float) (x + halfSize);
        float maxY = (float) (y + halfSize);
        float maxZ = (float) (z + halfSize);
        line(buffer, matrix, minX, minY, minZ, maxX, minY, minZ, alpha);
        line(buffer, matrix, maxX, minY, minZ, maxX, minY, maxZ, alpha);
        line(buffer, matrix, maxX, minY, maxZ, minX, minY, maxZ, alpha);
        line(buffer, matrix, minX, minY, maxZ, minX, minY, minZ, alpha);
        line(buffer, matrix, minX, maxY, minZ, maxX, maxY, minZ, alpha);
        line(buffer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, alpha);
        line(buffer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ, alpha);
        line(buffer, matrix, minX, maxY, maxZ, minX, maxY, minZ, alpha);
        line(buffer, matrix, minX, minY, minZ, minX, maxY, minZ, alpha);
        line(buffer, matrix, maxX, minY, minZ, maxX, maxY, minZ, alpha);
        line(buffer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, alpha);
        line(buffer, matrix, minX, minY, maxZ, minX, maxY, maxZ, alpha);
    }

    private static void line(VertexConsumer buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float alpha) {
        buffer.addVertex(matrix, x1, y1, z1).setColor(1f, 1f, 0f, alpha);
        buffer.addVertex(matrix, x2, y2, z2).setColor(1f, 1f, 0f, alpha);
    }

    private static void renderLabels(PoseStack poseStack, Camera camera) {
        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        for (SearchResult result : NAMED_RESULTS.values()) {
            scheduleLabel(result.pos().getCenter().add(result.nameOffset()), result.name(), false);
        }
        for (ScheduledLabel label : SCHEDULED_LABELS) {
            poseStack.pushPose();
            Vec3 pos = label.position.subtract(camera.getPosition());
            poseStack.translate(pos.x, pos.y, pos.z);
            poseStack.mulPose(camera.rotation());
            poseStack.scale(-0.025f, -0.025f, 0.025f);
            Matrix4f matrix = poseStack.last().pose();
            float x = -mc.font.width(label.text) / 2f;
            Font.DisplayMode mode = label.seeThrough ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL;
            RenderSystem.depthFunc(label.seeThrough ? GL11.GL_ALWAYS : GL11.GL_LEQUAL);
            mc.font.drawInBatch(label.text, x, 0, 0xFFFFFFFF, false, matrix, bufferSource, mode, 0, LightTexture.FULL_BRIGHT);
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            poseStack.popPose();
        }
        bufferSource.endBatch();
    }
}
