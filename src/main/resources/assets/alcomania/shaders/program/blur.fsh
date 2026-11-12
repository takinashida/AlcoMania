#version 150

/*
 * Fragment shader for the post-processing blur.
 *
 * For every screen pixel we take several neighbouring pixels along BlurDir
 * and average their colours. One pass blurs horizontally, the second pass
 * (see blur.json) blurs vertically. Together they give a cheap box blur.
 *
 * NOTE: comments in .vsh/.fsh are intentionally ASCII-only, because GLSL
 * drivers are not required to accept non-ASCII bytes even inside comments.
 *
 * Files:
 *   assets/alcomania/shaders/program/blur.fsh  (this file)
 *   assets/alcomania/shaders/program/blur.vsh
 *   assets/alcomania/shaders/program/blur.json (program description)
 *   assets/alcomania/shaders/post/blur.json    (post-effect chain)
 */

// The already rendered game frame.
uniform sampler2D DiffuseSampler;

// Current pixel coordinates (0..1) and the size of one texel (1 / screen width).
// oneTexel is calculated in the vertex shader and passed here.
in vec2 texCoord;
in vec2 oneTexel;

// Blur direction: (1, 0) = horizontal, (0, 1) = vertical.
uniform vec2 BlurDir;

// Blur radius in pixels. Bigger radius = stronger blur.
uniform float Radius;

// Output colour of this pixel.
out vec4 fragColor;

void main() {
    vec4 sum = vec4(0.0);

    // Add the colours of pixels from -Radius to +Radius along BlurDir.
    for (float offset = -Radius; offset <= Radius; offset += 1.0) {
        sum += texture(DiffuseSampler, texCoord + oneTexel * offset * BlurDir);
    }

    // Number of samples: radius on both sides plus the centre pixel.
    float samples = Radius * 2.0 + 1.0;

    // Average the sum to get the blurred colour (and the average alpha).
    fragColor = sum / samples;
}
