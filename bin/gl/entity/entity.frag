#version 150

in vec2 pass_uvs;
in vec3 pass_normals;

uniform sampler2D diffuse;
uniform vec3 lightDirection;
uniform vec4 color;

out vec4 out_color;

const float lightDiffuse = 0.5;
const float lightScale = 0.3;

void main(void) {
	vec4 diffuse = texture(diffuse, pass_uvs);
	if (diffuse.a < 0.1)
		discard;

	float ambientLight = lightDiffuse + (lightDirection.y*lightScale);

	float light = (dot(vec3(lightDirection.x, 0, lightDirection.z), pass_normals)*lightScale) + ambientLight;
	
	if (lightDirection.y < 0.0) {
		light /= 1.0-lightDirection.y;
	}
	
	out_color = (diffuse * light) * color;
}
