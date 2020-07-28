#version 150

in vec4 pass_uvs;
in vec3 pass_normals;
in vec4 pass_colors;
in vec4 shadowCoords;

uniform sampler2D terrainTexture;
uniform sampler2D depthTexture;

uniform vec3 lightDirection;
uniform int pcf;
uniform int mapSize;

out vec4 out_color;

const float lightDiffuse = 0.5;
const float lightScale = 0.3;

void main(void) {
	float shadow = 0.0;
	if (mapSize != 0) {
		float texelCount = (pcf * 2.0 + 1.0) * (pcf * 2.0 + 1.0);
		float texelSize = 1.0 / mapSize;
		
		for(int x = -pcf; x <= pcf; x++) {
			for(int y = -pcf; y <= pcf; y++) {
				float nearestLight = texture(depthTexture, shadowCoords.xy + vec2(x,y) * texelSize).r;
				if (shadowCoords.z > nearestLight + .003) {
					shadow += 1.0;
				}
			}
		}
		shadow /= texelCount;
	}
	
	float shadowShading = 1.0 - (shadow * shadowCoords.w * (0.45));
	float ambientLight = lightDiffuse + (lightDirection.y * lightScale);

	float light = (dot(vec3(lightDirection.x, 0, lightDirection.z), pass_normals) * lightScale) + ambientLight;
	vec4 tex = texture(terrainTexture, pass_uvs.xy);
	
	light = min(light, shadowShading);
	
	if (tex.a < 0.5) discard;
	out_color = (tex * pass_colors) * light;
}
