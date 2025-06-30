#version 330 core
out vec4 FragColor;

in vec3 Normal;
in vec3 FragPos;
in vec2 TexCoord;

struct Material {
    sampler2D diffuse;
    sampler2D specular;
    float shininess;
};

struct DirLight {
    vec3 direction;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

struct PointLight {
    vec3 position;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;

    float constant;
    float linear;
    float quadratic;
};

struct SpotLight {
    vec3 position;
    vec3 direction;
    float cutOff;
    float outerCutOff;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;

    float constant;
    float linear;
    float quadratic;
};

uniform vec3 viewPos;
uniform Material material;
uniform DirLight dirLight;
#define NR_POINT_LIGHTS 4
uniform PointLight pointLights[NR_POINT_LIGHTS];
uniform SpotLight spotLight;

vec3 CalcDirLight(DirLight light, vec3 normal, vec3 viewDir)
{
    vec3 lightDir = normalize(-light.direction);
    float diff = max(dot(normal, lightDir), 0.0);
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);

    vec3 box = texture(material.diffuse, TexCoord).rgb;
    vec3 spec_box = texture(material.specular, TexCoord).rgb;

    vec3 ambient = light.ambient * box;
    vec3 diffuse = light.diffuse * diff * box;
    vec3 specular = light.specular * spec * spec_box;

    return ambient + diffuse + specular;
}

vec3 CalcPointLight(PointLight light, vec3 normal, vec3 fragPos, vec3 viewDir)
{
    vec3 lightDir = normalize(light.position - fragPos);
    float diff = max(dot(normal, lightDir), 0.0);
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);

    float d = length(light.position - fragPos);
    float attenuation = 1.0 / (light.constant + light.linear * d + light.quadratic * d * d);

    vec3 box = texture(material.diffuse, TexCoord).rgb;
    vec3 spec_box = texture(material.specular, TexCoord).rgb;

    vec3 ambient = light.ambient * box;
    vec3 diffuse = light.diffuse * diff * box;
    vec3 specular = light.specular * spec * spec_box;

    return (ambient + diffuse + specular) * attenuation;
}    

vec3 CalcSpotLight(SpotLight light, vec3 normal, vec3 fragPos, vec3 viewDir)
{
    vec3 box = texture(material.diffuse, TexCoord).rgb;

    vec3 lightDir = normalize(light.position - fragPos);

    float theta = dot(lightDir, normalize(-light.direction));
    float epsilon = light.cutOff - light.outerCutOff;
    float intensity = clamp((theta - light.outerCutOff) / epsilon, 0.0, 1.0);

    float d = length(light.position - fragPos);
    float attenuation = 1.0 / (light.constant + light.linear * d + light.quadratic * d * d);

    vec3 ambientLight = light.ambient * box;

    float angle = max(dot(normal, lightDir), 0.0);
    vec3 diffuseLight = light.diffuse * angle * box * attenuation * intensity;

    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
    vec3 specularLight = light.specular * spec * vec3(texture(material.specular, TexCoord)) * attenuation * intensity;

    return ambientLight + diffuseLight + specularLight;
}

void main()
{
    vec3 norm = normalize(Normal);
    vec3 viewDir = normalize(viewPos - FragPos);
    vec3 result = CalcDirLight(dirLight, norm, viewDir);

    for(int i = 0; i < NR_POINT_LIGHTS; i++)
        result += CalcPointLight(pointLights[i], norm, FragPos, viewDir);

    result += CalcSpotLight(spotLight, norm, FragPos, viewDir);

    FragColor = vec4(result, 1.0);
}
