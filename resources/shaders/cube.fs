#version 330 core
out vec4 FragColor;

uniform vec3 objectColor;
uniform vec3 lightColor;

void main()
{
    vec3 ambientLight = lightColor * 0.1;
    FragColor = vec4(ambientLight * objectColor, 1.0);
}
