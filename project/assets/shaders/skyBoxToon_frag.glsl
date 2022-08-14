#version 330 core
out vec4 FragColor;

in vec3 TexCoords;

uniform samplerCube skybox;

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

void main()
{
    vec3 color = texture(skybox, TexCoords).rgb;
    float colorAVG = (color.r + color.g + color.b)/3;


    float gamma = 1.8;

    FragColor = vec4(pow((color * cellValue(colorAVG, 20)), vec3(1.0/gamma)),1);


}