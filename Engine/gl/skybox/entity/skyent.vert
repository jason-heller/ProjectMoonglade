#version 150

in vec2 in_vertices;
in vec2 in_uvs;

out vec2 pass_uvs;

uniform mat4 projectionViewMatrix;
uniform mat4 modelMatrix;


void main(void) {
	gl_Position = projectionViewMatrix * modelMatrix * vec4(in_vertices.xy, 0.0, 1.0);

	pass_uvs = in_uvs;
}
