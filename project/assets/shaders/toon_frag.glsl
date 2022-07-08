#version 330 core

//input from vertex shader
in struct VertexData
{
    vec3 toCamera;
    vec3 toPointLight;
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
uniform vec3 spotLightColor;
uniform vec2 spotLightAngle;
uniform vec3 spotLightAttParam;
uniform vec3 spotLightDir;
uniform vec3 farbe;

uniform vec3 dirLightDir;
uniform vec3 dirLightCol;

float gamma = 2.2;
vec3 texGammaCorrection (vec3 texture) {
    return pow(texture, vec3(gamma));
}

vec3 gammaCorrection (vec3 result) {
    return pow(result, vec3(1/gamma));
}

float cellValue(float value, int numberOfLvls) {
    float cell;
    float currentLvl = numberOfLvls;
    float step = 1.0 / numberOfLvls;
    while (currentLvl > 0) {
        if (value > currentLvl - step) {
            cell = currentLvl;
            break;
        }
        currentLvl -= step;
    }
    if (value == 0) cell = 0;
    return cell;
}



vec3 shade(vec3 n, vec3 l, vec3 v, vec3 dif, vec3 spec, float shine) {
    vec3 reflectDir = reflect(-l, n);
    float nl = dot(n,l);
    float vr = dot(v, reflectDir);
    vec3 diffuse = dif * cellValue(nl,10);
    float cosb = max(0.0, cellValue(vr,15));
    vec3 speculr = spec * pow(cosb, shine);

    return diffuse + speculr;
}

vec3 shadeS(vec3 n, vec3 l, vec3 v, vec3 dif, vec3 spec, float shine) {
    vec3 diffuse = dif * max(0.0, dot(n, l));
    vec3 reflectDir = reflect(-l, n);
    float cosb = max(0.0, dot(v, reflectDir));
    vec3 speculr = spec * pow(cosb, shine);

    return diffuse + speculr;
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
    float cellIntensity;
    float intensity = clamp((cosTheta - cosGamma)/(cosPhi - cosGamma), 0.0, 1.0);

    return spotLightColour * cellValue(intensity,4) * cellValue(attenuate(len, attParam),5);
}

void main() {

    vec3 n = normalize(vertexData.normale);
    vec3 v = normalize(vertexData.toCamera);
    float lpLength = length(vertexData.toPointLight);
    vec3 lp = vertexData.toPointLight/lpLength;
    float spLength = length(vertexData.toSpotLight);
    vec3 sp = vertexData.toSpotLight/spLength;
    vec3 diffCol = texGammaCorrection(texture(diff, vertexData.tc).xyz);
    vec3 emitCol = texGammaCorrection(texture(emit, vertexData.tc).xyz);
    vec3 specularCol = texGammaCorrection(texture(specular, vertexData.tc).xyz);
    vec3 dLd = normalize(-dirLightDir);


    float emitColAVG = (emitCol.r + emitCol.g + emitCol.z)/3;

    //emissive
    vec3 result = cellValue(emitColAVG,10) * emitCol * farbe;

    //
    //DirLight
    result += dirLightCol * shade(n, dLd, v, diffCol, specularCol, shininess);
    //Pointlight
    result += shade(n, lp, v, diffCol, specularCol, shininess) *
    pointLightIntensity(pointLightColor, lpLength, pointLightAttParam);


    //Spotlight
    result += shadeS(n, sp, v, diffCol, specularCol, shininess) *
    spotLightIntensity(spotLightColor, spLength, sp, spotLightDir, spotLightAttParam);



    color = vec4(gammaCorrection(result), 1.0);

}