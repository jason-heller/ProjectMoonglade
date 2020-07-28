#version 150

in vec3 in_vertices;
in vec2 in_uvs;
in vec3 in_normals;
in vec4 in_colors;

out vec4 pass_uvs;
out vec3 pass_normals;
out vec4 pass_colors;
out vec4 shadowCoords;

uniform mat4 toShadowSpace;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform float shadowDistance;

const float shadowDensity = 0.003;
const float shadowGradient = 5.0;
const float shadowTransition = 10.0;

void main(void) {

	vec4 worldPos = vec4(in_vertices.x, in_vertices.y, in_vertices.z, 1.0);
	vec4 camSpace = viewMatrix * worldPos;
	
	float distance = length(camSpace.xyz);
	shadowCoords = toShadowSpace * worldPos;
	distance = distance - (shadowDistance - shadowTransition);
	distance = distance / shadowTransition;
	shadowCoords.w = clamp(1.0 - distance, 0.0, 1.0);
	
	gl_Position = projectionMatrix * camSpace;
	
	if (in_normals.y <= 0.0) {
		pass_uvs = vec4(in_uvs.xy, 0.0, 0.0);
	} else {
		float tex_dx = fract(in_colors.w);
		pass_uvs = vec4(in_uvs.xy, tex_dx, (in_colors.w - tex_dx) / 100.0);
	}
	
	pass_normals = in_normals;
	pass_colors = in_colors;
	
}
