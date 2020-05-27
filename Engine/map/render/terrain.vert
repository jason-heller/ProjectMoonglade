#version 150

in vec3 in_vertices;
in vec2 in_uvs;
in vec3 in_normals;
in vec4 in_colors;

out vec4 pass_uvs;
out vec3 pass_normals;
out vec4 pass_colors;

uniform mat4 projectionViewMatrix;
uniform vec4 clipSpace;

void main(void) {

	vec4 worldPos = vec4(in_vertices.x, in_vertices.y, in_vertices.z, 1.0);
	gl_Position = projectionViewMatrix * worldPos;

	gl_ClipDistance[0] = dot(worldPos, clipSpace);
	
	if (in_normals.y <= 0.0) {
		pass_uvs = vec4(in_uvs.xy, 0.0, 0.0);
		pass_colors = vec4(0.3, 0.3, 0.4, 1.0);
	}
	else {
		float tex_dx = fract(in_colors.w);
		pass_uvs = vec4(in_uvs.xy, tex_dx, (in_colors.w - tex_dx)/100.0);
		pass_colors = vec4(in_colors.xyz, 1.0);
	}
	pass_normals = in_normals;
}
