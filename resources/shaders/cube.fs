#version 330 core
out vec4 FragColor;

uniform vec3 objectColor;
uniform vec3 lightColor;
uniform vec3 lightPos;

in vec3 Normal;
in vec3 FragPos;

void main()
{
    vec3 norm = normalize(Normal);
    vec3 lightDir = normalize(lightPos - FragPos);
    vec3 diffuseLight = max(dot(norm, lightDir), 0.0) * lightColor;

    vec3 ambientLight = lightColor * 0.1;

    FragColor = vec4((ambientLight + diffuseLight) * objectColor, 1.0);
}
