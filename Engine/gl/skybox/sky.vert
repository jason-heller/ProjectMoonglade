#version 150

in vec3 in_position;
out vec3 pass_uvs;
out float pass_bgAlpha;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform float bgAlpha;

void main(void){
	pass_bgAlpha = bgAlpha;
	gl_Position = projectionMatrix * viewMatrix * vec4(in_position, 1.0);
	pass_uvs = vec3(in_position.x,in_position.y,in_position.z);

}
