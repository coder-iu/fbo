#version 300 es
precision mediump float;

in vec2 vTextureCo;
uniform sampler2D uTextureSampler;
out vec4 fragColor;

void main() {
    fragColor = texture(uTextureSampler, vTextureCo);
}
