#version 150

in vec4 pass_uvs;
in vec3 pass_normals;
in vec4 pass_colors;

uniform sampler2D terrainTexture;

uniform vec3 lightDirection;

out vec4 out_color;

const float lightDiffuse = 0.5;
const float lightScale = 0.3;

void main(void) {
	float ambientLight = lightDiffuse + (lightDirection.y*lightScale);

	float light = (dot(vec3(lightDirection.x, 0, lightDirection.z), pass_normals)*lightScale) + ambientLight;
	vec4 tex = texture(terrainTexture, pass_uvs.xy);
	
	if (pass_colors.a == 1.0) {
		out_color = tex * pass_colors * light;
	} else {
		if (tex.a < 0.5) discard;
		out_color = tex * light;
	}
}
