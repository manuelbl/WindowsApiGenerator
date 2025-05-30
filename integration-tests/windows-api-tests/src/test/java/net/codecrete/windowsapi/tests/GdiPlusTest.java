//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.tests;

import org.junit.jupiter.api.Test;
import windows.win32.graphics.gdiplus.GdiplusStartupInput;
import windows.win32.graphics.gdiplus.GdiplusStartupOutput;
import windows.win32.graphics.gdiplus.ImageCodecInfo;
import windows.win32.graphics.gdiplus.SmoothingMode;
import windows.win32.graphics.gdiplus.Status;
import windows.win32.graphics.gdiplus.Unit;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static java.lang.foreign.MemorySegment.NULL;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static windows.win32.graphics.gdiplus.Apis.GdipCreateBitmapFromScan0;
import static windows.win32.graphics.gdiplus.Apis.GdipCreatePen1;
import static windows.win32.graphics.gdiplus.Apis.GdipCreateSolidFill;
import static windows.win32.graphics.gdiplus.Apis.GdipDeleteBrush;
import static windows.win32.graphics.gdiplus.Apis.GdipDeleteGraphics;
import static windows.win32.graphics.gdiplus.Apis.GdipDeletePen;
import static windows.win32.graphics.gdiplus.Apis.GdipDisposeImage;
import static windows.win32.graphics.gdiplus.Apis.GdipDrawArcI;
import static windows.win32.graphics.gdiplus.Apis.GdipFillEllipseI;
import static windows.win32.graphics.gdiplus.Apis.GdipFillRectangleI;
import static windows.win32.graphics.gdiplus.Apis.GdipGetImageEncoders;
import static windows.win32.graphics.gdiplus.Apis.GdipGetImageEncodersSize;
import static windows.win32.graphics.gdiplus.Apis.GdipGetImageGraphicsContext;
import static windows.win32.graphics.gdiplus.Apis.GdipSaveImageToFile;
import static windows.win32.graphics.gdiplus.Apis.GdipSetSmoothingMode;
import static windows.win32.graphics.gdiplus.Apis.GdipSetSolidFillColor;
import static windows.win32.graphics.gdiplus.Apis.GdiplusShutdown;
import static windows.win32.graphics.gdiplus.Apis.GdiplusStartup;

class GdiPlusTest {

    @Test
    void drawToOffscreenBitmap() {
        try (var arena = Arena.ofConfined()) {
            var startupInput = GdiplusStartupInput.allocate(arena);
            GdiplusStartupInput.GdiplusVersion(startupInput, 1);
            var startupOutput = GdiplusStartupOutput.allocate(arena);
            var token = arena.allocate(JAVA_LONG);
            var status = GdiplusStartup(token, startupInput, startupOutput);
            assertThat(status).isEqualTo(Status.Ok);

            final var width = 600;
            final var height = 600;
            final var stride = width * 4;
            var buffer = arena.allocate(width * 4 * height);
            var bitmapHolder = arena.allocate(ADDRESS);
            status = GdipCreateBitmapFromScan0(width, height, stride, 2498570, buffer, bitmapHolder);
            assertThat(status).isEqualTo(Status.Ok);

            var bitmap = bitmapHolder.get(ADDRESS, 0);
            var graphicsHolder = arena.allocate(ADDRESS);
            status = GdipGetImageGraphicsContext(bitmap, graphicsHolder);
            assertThat(status).isEqualTo(Status.Ok);

            var graphics = graphicsHolder.get(ADDRESS, 0);

            var brushHolder = arena.allocate(ADDRESS);
            status = GdipCreateSolidFill(0xffffffff, brushHolder);
            assertThat(status).isEqualTo(Status.Ok);
            var brush = brushHolder.get(ADDRESS, 0);

            status = GdipFillRectangleI(graphics, brush, 0, 0, 600, 600);
            assertThat(status).isEqualTo(Status.Ok);

            status = GdipSetSmoothingMode(graphics, SmoothingMode.SmoothingModeAntiAlias);
            assertThat(status).isEqualTo(Status.Ok);

            drawShape(graphics, brush);

            status = GdipDeleteBrush(brush);
            assertThat(status).isEqualTo(Status.Ok);

            var encoderClsid = getPngEncoder(arena);

            var filename = arena.allocateFrom("mickey.png", UTF_16LE);
            status = GdipSaveImageToFile(bitmap, filename, encoderClsid, NULL);
            assertThat(status).isEqualTo(Status.Ok);

            status = GdipDeleteGraphics(graphics);
            assertThat(status).isEqualTo(Status.Ok);

            status = GdipDisposeImage(bitmap);
            assertThat(status).isEqualTo(Status.Ok);

            GdiplusShutdown(token.get(JAVA_LONG, 0));
        }
    }

    void drawShape(MemorySegment graphics, MemorySegment brush) {
        fillEllipse(graphics, brush, 179, 180, 73, 73, 0xff000000);
        fillEllipse(graphics, brush, 422, 180, 73, 73, 0xff000000);
        fillEllipse(graphics, brush, 300, 328, 131, 131, 0xff000000);
        fillEllipse(graphics, brush, 300, 328, 127, 127, 0xffe6c6ab);
        fillEllipse(graphics, brush, 334, 294, 24, 49, 0xffffffff);
        fillEllipse(graphics, brush, 266, 294, 24, 49, 0xffffffff);
        fillEllipse(graphics, brush, 335, 314, 17, 20, 0xff000000);
        fillEllipse(graphics, brush, 267, 314, 17, 20, 0xff000000);
        fillEllipse(graphics, brush, 300, 373, 28, 23, 0xff000000);

        try (var arena = Arena.ofConfined()) {
            var penHolder = arena.allocate(ADDRESS);

            var status = GdipCreatePen1(0xff000000, 5, Unit.UnitPixel, penHolder);
            assertThat(status).isEqualTo(Status.Ok);
            var pen = penHolder.get(ADDRESS, 0);

            status = GdipDrawArcI(graphics, pen, 220, 304, 162, 123, 25, 130);
            assertThat(status).isEqualTo(Status.Ok);

            status = GdipDeletePen(pen);
            assertThat(status).isEqualTo(Status.Ok);
        }
    }

    void fillEllipse(MemorySegment graphics, MemorySegment brush, int cx, int cy, int rx, int ry, int color) {
        var status = GdipSetSolidFillColor(brush, color);
        assertThat(status).isEqualTo(Status.Ok);

        status = GdipFillEllipseI(graphics, brush, cx - rx, cy - ry, rx + rx, ry + ry);
        assertThat(status).isEqualTo(Status.Ok);
    }

    MemorySegment getPngEncoder(Arena arena) {
        var numEncodersHolder = arena.allocate(JAVA_INT);
        var sizeHolder = arena.allocate(JAVA_INT);
        var status = GdipGetImageEncodersSize(numEncodersHolder, sizeHolder);
        assertThat(status).isEqualTo(Status.Ok);

        var numEncoders = numEncodersHolder.get(JAVA_INT, 0);
        var size = sizeHolder.get(JAVA_INT, 0);

        var encoders = arena.allocate(size);
        status = GdipGetImageEncoders(numEncoders, size, encoders);
        assertThat(status).isEqualTo(Status.Ok);

        for (int i = 0; i < numEncoders; i++) {
            var info = ImageCodecInfo.elementAsSlice(encoders, i);
            var mimeType = ImageCodecInfo.MimeType(info);
            if ("image/png".equals(mimeType.getString(0, UTF_16LE)))
                return ImageCodecInfo.Clsid(info);
        }

        fail("encoder not found");
        throw new AssertionError();
    }
}
