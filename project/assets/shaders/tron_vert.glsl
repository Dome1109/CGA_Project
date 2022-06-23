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
uniform vec3 spotLightPos;

out struct VertexData
{
    vec3 toCamera;
    vec3 toPointLight;
    vec3 toSpotLight;
    vec2 tc;
    vec3 normale;
} vertexData;

//
void main(){
    mat4 modelview = view * model_matrix;
    vec4 modelViewPosition = modelview * vec4(position, 1.0f);

    vec4 pos = projection * modelViewPosition;
    vec4 norm = transpose(inverse(modelview)) * vec4(normals, 0.0f);
    gl_Position = pos;
    vertexData.normale = norm.xyz;
    vertexData.tc =  texCoord * tcMultiplier;

    vec4 lp = view * vec4(pointLightPos, 1.0);
    vertexData.toPointLight = (lp - modelViewPosition).xyz;

    vec4 lp2 = view * vec4(spotLightPos, 1.0);
    vertexData.toSpotLight = (lp2 - modelViewPosition).xyz;

    vertexData.toCamera = -modelViewPosition.xyz;
}
