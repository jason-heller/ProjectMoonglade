#version 150

in vec2 pass_uvs;
in vec3 pass_normals;

uniform sampler2D diffuse;
uniform vec3 lightDirection;
uniform vec3 color;

out vec4 out_color;

const float lightDiffuse = 0.6;
const float lightScale = 1.0 - lightDiffuse;

void main(void) {
	vec4 diffuse = texture(diffuse, pass_uvs);
	if (diffuse.a < 0.1)
		discard;

	float light = dot(lightDirection, pass_normals)*lightScale + lightDiffuse;
	
	if (lightDirection.y < 0.0) {
		light /= 1.0-lightDirection.y;
	}
	
	out_color = (diffuse * light) * vec4(color.xyz, 1.0);
}
