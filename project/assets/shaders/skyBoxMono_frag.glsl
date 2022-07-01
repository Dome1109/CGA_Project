#version 330 core
out vec4 FragColor;

in vec3 TexCoords;

uniform samplerCube skybox;



void main()
{
    vec4 color = texture(skybox, TexCoords);

    FragColor = vec4((color.s+ color.t+ color.p)/3,0,0,0);

}