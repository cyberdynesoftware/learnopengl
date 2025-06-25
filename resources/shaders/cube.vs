#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;

out vec3 Normal;
out vec3 FragPos;
out vec3 Gouraud;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

uniform vec3 lightColor;
uniform vec3 viewPos;
uniform vec3 lightPos;

void main()
{
    gl_Position = projection * view * model * vec4(aPos, 1.0f);
    Normal = aNormal;
    FragPos = vec3(model * vec4(aPos, 1.0));

    vec3 ambientLight = lightColor * 0.1;

    vec3 norm = normalize(aNormal);
    vec3 lightDir = normalize(lightPos - vec3(model * vec4(aPos, 1.0)));
    vec3 diffuseLight = max(dot(norm, lightDir), 0.0) * lightColor;

    vec3 viewDir = normalize(viewPos - vec3(model * vec4(aPos, 1.0)));
    vec3 reflectDir = reflect(-lightDir, norm);
    vec3 specularLight = pow(max(dot(viewDir, reflectDir), 0.0), 32) * lightColor * 0.5;
    Gouraud = ambientLight + diffuseLight + specularLight;
}
