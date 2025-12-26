#version 300 es
in vec4 aPosition;
in vec2 aTextureCo;

out vec2 vTextureCo;

void main() {
    gl_Position = aPosition;
    vTextureCo = aTextureCo;
}
