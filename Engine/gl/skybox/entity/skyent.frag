#version 150

in vec2 pass_uvs;

uniform sampler2D diffuse;

out vec4 out_color;

void main(void) {
	vec4 diffuse = texture(diffuse, pass_uvs);
	if (diffuse.a < 0.1)
		discard;

	out_color = diffuse;
}
