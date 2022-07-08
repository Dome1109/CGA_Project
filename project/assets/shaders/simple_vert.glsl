#version 330 core

layout(location = 0) in vec3 position;

uniform mat4 lightProjection;
uniform mat4 model_matrix;

void main(){

    gl_Position = lightProjection * model_matrix * vec4(position, 1f);


}
