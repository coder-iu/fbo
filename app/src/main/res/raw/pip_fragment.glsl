#version 300 es
precision mediump float;

in vec2 vTextureCo;
uniform sampler2D uTextureSampler;  // FBO纹理，使用sampler2D
out vec4 fragColor;

void main() {
    vec2 uv = vTextureCo;

    // 4个画中画，从大到小重叠显示
    // 层1（最底层，最大）：全屏
    // 层2：75%大小，居中偏左
    // 层3：50%大小，居中偏右
    // 层4（最上层，最小）：25%大小，居中

    // 检查顺序从最上层到最下层
    vec4 finalColor;

    // 1. 最上层画中画（最小，25%大小，居中）
    vec2 pip4Center = vec2(0.5, 0.5);
    vec2 pip4Size = vec2(0.25, 0.25);
    vec2 pip4Min = pip4Center - pip4Size * 0.5;
    vec2 pip4Max = pip4Center + pip4Size * 0.5;

    if (uv.x >= pip4Min.x && uv.x <= pip4Max.x &&
        uv.y >= pip4Min.y && uv.y <= pip4Max.y) {
        // 在最小画中画内
        vec2 pipUV = (uv - pip4Min) / pip4Size;
        finalColor = texture(uTextureSampler, pipUV);
        fragColor = finalColor;
        return;
    }

    // 2. 第三层画中画（50%大小，右上角）
    vec2 pip3Center = vec2(0.7, 0.3);
    vec2 pip3Size = vec2(0.5, 0.5);
    vec2 pip3Min = pip3Center - pip3Size * 0.5;
    vec2 pip3Max = pip3Center + pip3Size * 0.5;

    if (uv.x >= pip3Min.x && uv.x <= pip3Max.x &&
        uv.y >= pip3Min.y && uv.y <= pip3Max.y) {
        vec2 pipUV = (uv - pip3Min) / pip3Size;
        finalColor = texture(uTextureSampler, pipUV);
        fragColor = finalColor;
        return;
    }

    // 3. 第二层画中画（75%大小，左下角）
    vec2 pip2Center = vec2(0.3, 0.7);
    vec2 pip2Size = vec2(0.75, 0.75);
    vec2 pip2Min = pip2Center - pip2Size * 0.5;
    vec2 pip2Max = pip2Center + pip2Size * 0.5;

    if (uv.x >= pip2Min.x && uv.x <= pip2Max.x &&
        uv.y >= pip2Min.y && uv.y <= pip2Max.y) {
        vec2 pipUV = (uv - pip2Min) / pip2Size;
        finalColor = texture(uTextureSampler, pipUV);
        fragColor = finalColor;
        return;
    }

    // 4. 最底层画中画（100%大小，全屏）
    finalColor = texture(uTextureSampler, uv);
    fragColor = finalColor;
}