#version 330 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec2 texCoord;
layout(location = 2) in vec3 normals;

//uniforms
uniform mat4 model_matrix;
uniform mat4 view;
uniform mat4 projection;
uniform vec2 tcMultiplier;
uniform vec3 pointLightPos;
uniform vec3 pointLight2Pos;
uniform vec3 pointLight3Pos;
uniform vec3 spotLightPos;
uniform mat3 textureTransform;

out struct VertexData
{
    vec3 toCamera;
    vec3 toPointLight;
    vec3 toPointLight2;
    vec3 toPointLight3;
    vec3 toSpotLight;
    vec2 tc;
    vec3 normale;

} vertexData;

//
void main(){
    mat4 modelview = view * model_matrix;

    vec4 modelViewPosition = modelview * vec4(position, 1.0);

    vec4 pos = projection * modelViewPosition;
    vec4 norm = transpose(inverse(modelview)) * vec4(normals, 0.0);
    gl_Position = pos;
    vertexData.normale = norm.xyz;

    vec3 transformedTC = textureTransform * vec3(texCoord, 1.0);
    vertexData.tc = vec2(transformedTC) * tcMultiplier;

    vec4 lp = view * vec4(pointLightPos, 1.0);
    vec4 lp2 = view * vec4(pointLight2Pos, 1.0);
    vec4 lp3 = view * vec4(pointLight3Pos, 1.0);
    vertexData.toPointLight = (lp - modelViewPosition).xyz;
    vertexData.toPointLight2 = (lp2 - modelViewPosition).xyz;
    vertexData.toPointLight3 = (lp3 - modelViewPosition).xyz;

    vec4 lpS = view * vec4(spotLightPos, 1.0);
    vertexData.toSpotLight = (lpS - modelViewPosition).xyz;

    vertexData.toCamera = -modelViewPosition.xyz;
}
