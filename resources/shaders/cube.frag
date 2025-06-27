#version 330 core
out vec4 FragColor;

in vec3 Normal;
in vec3 FragPos;
in vec2 TexCoord;

struct Material {
    sampler2D diffuse;
    sampler2D specular;
    sampler2D emission;
    float shininess;
};

struct Light {
    vec3 position;
    vec3 direction;
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

uniform vec3 viewPos;
uniform Material material;
uniform Light light;

void main()
{
    vec3 boxTexture = vec3(texture(material.diffuse, TexCoord));
    vec3 emissionMap = vec3(texture(material.emission, TexCoord));

    vec3 ambientLight = light.ambient * boxTexture;

    vec3 norm = normalize(Normal);
    //vec3 lightDir = normalize(light.position - FragPos);
    vec3 lightDir = normalize(-light.direction);
    float angle = max(dot(norm, lightDir), 0.0);
    vec3 diffuseLight = light.diffuse * angle * boxTexture;

    vec3 viewDir = normalize(viewPos - FragPos);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
    vec3 specularLight = light.specular * spec * vec3(texture(material.specular, TexCoord));

    FragColor = vec4((ambientLight + diffuseLight + specularLight), 1.0);
}
