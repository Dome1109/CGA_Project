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
    vec3 x = lightColor * attenuate(len, attParam);
    if (x.x > 0.95 && x.y > 0.95 && x.z >0.95)   lightColor *= vec3(1.0, 1.0, 1.0);
    else if (x.x > 0.75 && x.y > 0.75 && x.z >0.75)  lightColor *= vec3(0.8, 0.8, 0.8);
    else if (x.x > 0.50 && x.y > 0.5 && x.z >0.5) lightColor *= vec3(0.6, 0.6, 0.6);
    else if (x.x > 0.25 && x.y > 0.25 && x.z >0.25) lightColor *= vec3(0.4, 0.4, 0.4);
    else if (x.x > 0.0 && x.y > 0.0 && x.z >0.0)  lightColor *= vec3(0.2, 0.2, 0.2);
    return lightColor * attenuate(len, attParam);
}

vec3 spotLightIntensity(vec3 spotLightColour, float len, vec3 sp, vec3 spDir, vec3 attParam) {
    float cosTheta = dot(sp, normalize(spDir));
    float cosPhi = cos(spotLightAngle.x);
    float cosGamma = cos(spotLightAngle.y);

    float intensity = clamp((cosTheta - cosGamma)/(cosPhi - cosGamma), 0.0, 1.0);

    if (intensity > 0.95)   spotLightColour *= vec3(1.0, 1.0, 1.0);
    else if (intensity > 0.75)  spotLightColour *= vec3(0.8, 0.8, 0.8);
    else if (intensity > 0.50) spotLightColour *= vec3(0.6, 0.6, 0.6);
    else if (intensity > 0.25) spotLightColour *= vec3(0.4, 0.4, 0.4);
    else if (intensity > 0.0)  spotLightColour *= vec3(0.2, 0.2, 0.2);

    return spotLightColour * intensity * attenuate(len, attParam);
}


void main() {

    vec3 n = normalize(vertexData.normale);
    vec3 v = normalize(vertexData.toCamera);
    float lpLength = length(vertexData.toPointLight);
    vec3 lp = vertexData.toPointLight/lpLength;
    float spLength = length(vertexData.toSpotLight);
    vec3 sp = vertexData.toSpotLight/spLength;

    // Stufen reinballern
    vec3 diffCol = texture(diff, vertexData.tc).xyz;
    if (diffCol.x > 0.95 && diffCol.y > 0.95 && diffCol.z >0.95)   diffCol *= vec3(1.0, 1.0, 1.0);
    else if (diffCol.x > 0.75 && diffCol.y > 0.75 && diffCol.z >0.75)  diffCol *= vec3(0.8, 0.8, 0.8);
    else if (diffCol.x > 0.50 && diffCol.y > 0.5 && diffCol.z >0.5) diffCol *= vec3(0.6, 0.6, 0.6);
    else if (diffCol.x > 0.25 && diffCol.y > 0.25 && diffCol.z >0.25) diffCol *= vec3(0.4, 0.4, 0.4);
    else if (diffCol.x > 0.0 && diffCol.y > 0.0 && diffCol.z >0.0)  diffCol *= vec3(0.2, 0.2, 0.2);

    vec3 emitCol = texture(emit, vertexData.tc).xyz;
    if (emitCol.x > 0.95 && emitCol.y > 0.95 && emitCol.z >0.95)   emitCol *= vec3(1.0, 1.0, 1.0);
    else if (emitCol.x > 0.75 && emitCol.y > 0.75 && emitCol.z >0.75)  emitCol *= vec3(0.8, 0.8, 0.8);
    else if (emitCol.x > 0.50 && emitCol.y > 0.5 && emitCol.z >0.5) emitCol *= vec3(0.6, 0.6, 0.6);
    else if (emitCol.x > 0.25 && emitCol.y > 0.25 && emitCol.z >0.25) emitCol *= vec3(0.4, 0.4, 0.4);
    else if (emitCol.x > 0.0 && emitCol.y > 0.0 && emitCol.z >0.0)  emitCol *= vec3(0.2, 0.2, 0.2);

    vec3 specularCol = texture(specular, vertexData.tc).xyz;
    if (specularCol.x > 0.95 && specularCol.y > 0.95 && specularCol.z >0.95)   specularCol *= vec3(1.0, 1.0, 1.0);
    else if (specularCol.x > 0.75 && specularCol.y > 0.75 && specularCol.z >0.75)  specularCol *= vec3(0.8, 0.8, 0.8);
    else if (specularCol.x > 0.50 && specularCol.y > 0.5 && specularCol.z >0.5) specularCol *= vec3(0.6, 0.6, 0.6);
    else if (specularCol.x > 0.25 && specularCol.y > 0.25 && specularCol.z >0.25) specularCol *= vec3(0.4, 0.4, 0.4);
    else if (specularCol.x > 0.0 && specularCol.y > 0.0 && specularCol.z >0.0)  specularCol *= vec3(0.2, 0.2, 0.2);


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

