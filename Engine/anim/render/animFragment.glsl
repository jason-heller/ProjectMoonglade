#version 330

const vec2 lightBias = vec2(0.7, 0.6);//just indicates the balance between diffuse and ambient lighting

in vec2 pass_textureCoords;
in vec3 pass_normal;

layout (location = 0) out vec4 out_color;
layout (location = 1) out vec4 out_brightness;

uniform sampler2D diffuse;
uniform sampler2D specular;
uniform float specularity;

in vec3 toCamera;
uniform vec3 lightDirection;

void main(void){
	
	vec4 diffuseTexture = texture(diffuse, pass_textureCoords);		
	vec3 unitNormal = normalize(pass_normal);
	float diffuseLight = max(dot(-lightDirection, unitNormal), 0.0) * lightBias.x + lightBias.y;
	float specularLight = 0.0;
	
	out_brightness = vec4(0.0);
	vec4 specularVec = vec4(0.0);
	if (specularity > 0.0) {
		vec4 specularTexture = texture(specular, pass_textureCoords);
		
		vec3 lightReflectDir = reflect(-lightDirection, unitNormal);
		float specularFactor = dot(lightReflectDir, toCamera);
		specularFactor = max(specularFactor, 0.0);
		
		float damped = pow(specularFactor, 10);
		specularLight = specularTexture.r*damped;
		
		specularVec = vec4(specularLight, specularLight, specularLight, 1.0);
		
		if (specularTexture.g > 0.0) {
			out_brightness = diffuseTexture + specularVec;
		}
	}
	
	out_color = specularVec + (diffuseTexture * diffuseLight);
}