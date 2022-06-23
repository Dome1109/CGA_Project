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

vec3 shade(vec3 n, vec3 l, vec3 v, vec3 dif, vec3 spec, float shine) {
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

    float intensity = clamp((cosTheta - cosGamma)/(cosPhi - cosGamma), 0.0, 1.0);

    return spotLightColour * intensity * attenuate(len, attParam);
}

void main() {

    vec3 n = normalize(vertexData.normale);
    vec3 v = normalize(vertexData.toCamera);
    float lpLength = length(vertexData.toPointLight);
    vec3 lp = vertexData.toPointLight/lpLength;
    float spLength = length(vertexData.toSpotLight);
    vec3 sp = vertexData.toSpotLight/spLength;
    vec3 diffCol = texture(diff, vertexData.tc).xyz;
    vec3 emitCol = texture(emit, vertexData.tc).xyz;
    vec3 specularCol = texture(specular, vertexData.tc).xyz;

    //emissive
    vec3 result = emitCol * farbe;

    //Pointlight
    result += shade(n, lp, v, diffCol, specularCol, shininess) *
        pointLightIntensity(pointLightColor, lpLength, pointLightAttParam);

    //Spotlight
    result += shade(n, sp, v, diffCol, specularCol, shininess) *
        spotLightIntensity(spotLightColor, spLength, sp, spotLightDir, spotLightAttParam);

    color = vec4(result, 1.0);

}