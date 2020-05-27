#version 150

in vec2 pass_textureCoords;
in vec4 clipSpace;

uniform sampler2D reflection;
uniform sampler2D refraction;
uniform sampler2D dudv;
//uniform vec3 lightDirection;
uniform float timer;

out vec4 out_color;

const vec2 lightBias = vec2(0.7, 0.6);//just indicates the balance between diffuse and ambient lighting

void main(void){
	vec2 normalizedDeviceSpace = (clipSpace.xy/clipSpace.w)/2.0 + 0.5;

	float scroll = timer / 50.0;
	vec2 offset = (texture(dudv, vec2(pass_textureCoords.x + scroll, pass_textureCoords.y + scroll)).rg*2.0 - 1.0) * .025;
	normalizedDeviceSpace+=offset;
	normalizedDeviceSpace = clamp(normalizedDeviceSpace, 0.001, 0.999);
	
	
	
	vec4 reflectionColor = texture(reflection, vec2(normalizedDeviceSpace.x, 1.0-normalizedDeviceSpace.y));	
	vec4 refractionColor = texture(refraction, normalizedDeviceSpace);	
	
	
	
	//vec3 newLightDir = -lightDirection;
	//if (newLightDir.y < -.65) {
	//	newLightDir.y = -.65;
	//}
	//float diffuseLight = max(dot(-newLightDir, vec3(0,1,0)), 0.0) * lightBias.x + lightBias.y;
	
	//if (lightDirection.y > .65) {
	//	diffuseLight -= lightDirection.y-.65;
	//}
	
	//out_color = reflectionColor;
	//
	out_color = mix(reflectionColor, refractionColor, 0.5);
	//out_color.a = 0.4;
	
}
