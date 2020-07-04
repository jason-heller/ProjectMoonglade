#version 150

in vec2 in_vertices;
in vec2 in_uvs;

out vec2 pass_uvs;
out vec3 pass_normals;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform vec3 uv;


void main(void) {
	gl_Position = projectionMatrix * viewMatrix * vec4(in_vertices.xy, 0.0, 1.0);

	pass_uvs = (in_uvs + uv.xy) * uv.z;
	pass_normals = vec3(0,1,0);
}
