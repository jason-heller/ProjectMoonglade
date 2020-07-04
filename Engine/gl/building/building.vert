#version 150

in vec3 in_vertices;
in vec2 in_uvs;
in vec3 in_normals;
in vec3 in_colors;

out vec2 pass_uvs;
out vec3 pass_normals;
out vec3 pass_colors;

uniform mat4 projectionViewMatrix;

void main(void) {

	gl_Position = projectionViewMatrix * vec4(in_vertices, 1.0);

	pass_uvs = in_uvs;
	pass_normals = in_normals;
	pass_colors = in_colors;
}
