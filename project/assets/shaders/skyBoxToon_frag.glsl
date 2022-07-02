#version 330 core
out vec4 FragColor;

in vec3 TexCoords;

uniform samplerCube skybox;



void main()
{
    vec3 color = texture(skybox, TexCoords).rgb;
    if (color.x > 0.95 && color.y > 0.95 && color.z >0.95)   color *= vec3(1.0, 1.0, 1.0);
    else if (color.x > 0.75 && color.y > 0.75 && color.z >0.75)  color *= vec3(0.8, 0.8, 0.8);
    else if (color.x > 0.50 && color.y > 0.5 && color.z >0.5) color *= vec3(0.6, 0.6, 0.6);
    else if (color.x > 0.25 && color.y > 0.25 && color.z >0.25) color *= vec3(0.4, 0.4, 0.4);
    else if (color.x > 0.0 && color.y > 0.0 && color.z >0.0)  color *= vec3(0.2, 0.2, 0.2);
    FragColor = vec4(color,1f);
}