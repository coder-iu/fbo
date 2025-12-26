#version 300 es
#extension GL_OES_EGL_image_external : require
precision mediump float;

in vec2 vTextureCo;
uniform samplerExternalOES uTextureSampler;
uniform mat4 uSTMatrix;
out vec4 fragColor;
void main() {
    vec4 videoTexCo = uSTMatrix * vec4(vTextureCo, 0.0, 1.0);
    fragColor = texture(uTextureSampler, videoTexCo.xy);
}
