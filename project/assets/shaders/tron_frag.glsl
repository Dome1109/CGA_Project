#version 330 core

//input from vertex shader
in struct VertexData
{
    vec3 toCamera;
    vec3 toPointLight;
    vec3 toPointLight2;
    vec3 toPointLight3;
    vec3 toPointLight4;
    vec3 toSpotLight;
    vec2 tc;
    vec3 normale;

} vertexData;

//fragment shader output
out vec4 color;

//Texture Uniforms
uniform sampler2D diff;
uniform sampler2D emit;
uniform sampler2D specular;

uniform float shininess;

//Light Uniforms
uniform vec3 pointLightColor;
uniform vec3 pointLightAttParam;
uniform vec3 pointLight2Color;
uniform vec3 pointLight2AttParam;
uniform vec3 pointLight3Color;
uniform vec3 pointLight3AttParam;
uniform vec3 pointLight4Color;
uniform vec3 pointLight4AttParam;
uniform vec3 spotLightColor;
uniform vec2 spotLightAngle;
uniform vec3 spotLightAttParam;
uniform vec3 spotLightDir;
uniform vec3 farbe;

uniform vec3 dirLightDir;
uniform vec3 dirLightCol;
uniform int blinn;


float gamma = 2.2;
vec3 texGammaCorrection (vec3 texture) {
    return pow(texture, vec3(gamma));
}

vec3 gammaCorrection (vec3 result) {
    return pow(result, vec3(1/gamma));
}
// n = normale, l = tolight, v, tocamera
vec3 shade(vec3 n, vec3 l, vec3 v, vec3 dif, vec3 spec, float shine) {
    vec3 diffuse = dif * max(0.0, dot(n, l));
    vec3 speculr;
    if (blinn == 1) {
        vec3 halfwayDir = normalize(l + v);
        speculr = spec * pow(max(0.0, dot(n, halfwayDir)), shine*2);
    }
    else {
        vec3 reflectDir = reflect(-l, n);
        float cosb = max(0.0, dot(v, reflectDir));
        speculr = spec * pow(cosb, shine);
    }


    return diffuse  + speculr;
}

float attenuate(float len, vec3 attParam) {
    return 1.0 / (attParam.x + attParam.y * len + attParam.z * len * len);
}

vec3 pointLightIntensity(vec3 lightColor, float len, vec3 attParam) {
    return lightColor * attenuate(len, attParam);
}

vec3 spotLightIntensity(vec3 spotLightColour, float len, vec3 sp, vec3 spDir, vec3 attParam) {
    float cosTheta = dot(sp, normalize(spDir));
    float cosPhi = cos(spotLightAngle.x);
    float cosGamma = cos(spotLightAngle.y);

    float intensity = clamp((cosTheta - cosGamma)/(cosPhi - cosGamma), 0.0, 1.0);

    return spotLightColour * intensity * attenuate(len, attParam);
}

void main() {

    vec3 n = normalize(vertexData.normale);
    vec3 v = normalize(vertexData.toCamera);
    float lpLength = length(vertexData.toPointLight);
    vec3 lp = vertexData.toPointLight/lpLength;
    float lp2Length = length(vertexData.toPointLight2);
    vec3 lp2 = vertexData.toPointLight2/lp2Length;
    float lp3Length = length(vertexData.toPointLight3);
    vec3 lp3 = vertexData.toPointLight3/lp3Length;
    float lp4Length = length(vertexData.toPointLight4);
    vec3 lp4 = vertexData.toPointLight4/lp4Length;
    float spLength = length(vertexData.toSpotLight);
    vec3 sp = vertexData.toSpotLight/spLength;
    vec3 diffCol = texGammaCorrection(texture(diff, vertexData.tc).xyz);
    vec3 emitCol = texGammaCorrection(texture(emit, vertexData.tc).xyz);
    vec3 specularCol = texGammaCorrection(texture(specular, vertexData.tc).xyz);
    vec3 dLd = normalize(-dirLightDir);
    //emissive
    vec3 result = emitCol * farbe;



    //DirLight
    result += dirLightCol * shade(n, dLd, v, diffCol, specularCol, shininess);

    //Pointlight3
    result += shade(n, lp3, v, diffCol, specularCol, shininess) *
    pointLightIntensity(pointLight3Color, lp3Length, pointLight3AttParam);

    //Pointlight4
    result += shade(n, lp4, v, diffCol, specularCol, shininess) *
    pointLightIntensity(pointLight4Color, lp4Length, pointLight4AttParam);

    //Pointlight2
    /*
    result += shade(n, lp2, v, diffCol, specularCol, shininess) *
    pointLightIntensity(pointLight2Color, lp2Length, pointLight2AttParam);

    //Pointlight
    result += shade(n, lp, v, diffCol, specularCol, shininess) *
    pointLightIntensity(pointLightColor, lpLength, pointLightAttParam);
    */


    //Spotlight
    result += shade(n, sp, v, diffCol, specularCol, shininess) *
    spotLightIntensity(spotLightColor, spLength, sp, spotLightDir, spotLightAttParam);


    color = vec4(gammaCorrection(result), 1.0);

}