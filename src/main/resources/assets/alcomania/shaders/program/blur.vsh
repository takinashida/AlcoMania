#version 150

/*
 * Vertex shader for the post-processing blur.
 *
 * Minecraft draws one full-screen quad. We pass its coordinates through and
 * calculate two values for the fragment shader:
 *   - texCoord: pixel coordinates in the input texture (0..1);
 *   - oneTexel: size of one texture pixel (1 / width, 1 / height), so the
 *     fragment shader can step to neighbouring pixels.
 *
 * NOTE: comments in .vsh/.fsh are intentionally ASCII-only, because GLSL
 * drivers are not required to accept non-ASCII bytes even inside comments.
 */

in vec4 Position;

uniform mat4 ProjMat;
uniform vec2 InSize;
uniform vec2 OutSize;

out vec2 texCoord;
out vec2 oneTexel;

void main() {
    // Convert the quad position into clip-space coordinates.
    vec4 outPos = ProjMat * vec4(Position.xy, 0.0, 1.0);
    gl_Position = vec4(outPos.xy, 0.2, 1.0);

    // Size of one texel of the input texture.
    oneTexel = 1.0 / InSize;

    // Position.xy is in the 0..OutSize range, so this gives 0..1.
    texCoord = Position.xy / OutSize;
}
