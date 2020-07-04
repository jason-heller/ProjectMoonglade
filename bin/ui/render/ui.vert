#version 150

in vec2 in_vertices;
in vec2 in_uvs;

out vec2 pass_uvs;

uniform vec4 translation;
uniform vec4 offset;
uniform bool centered;
uniform float rot;


void main(void){

	vec2 centering = vec2(0.5,0.5);

	if (centered) {
		centering = vec2(0, 1);
	}

	if (offset == vec4(0.0)) {
		pass_uvs = in_uvs;
	} else {
		switch(gl_VertexID) {
		case 0: pass_uvs = offset.xw; break;
		case 1: pass_uvs = offset.zw; break;
		case 2: pass_uvs = offset.xy; break;
		case 3: pass_uvs = offset.zy; break;
		}
	}

	mat2 rotationMatrix = mat2(
		cos(rot), -sin(rot),
		sin(rot),  cos(rot));
		
		
	if (rot == 0.0) {
		gl_Position = vec4((vec2((in_vertices) + centering)*translation.zw) + translation.xy * vec2(2.0, -2.0), 0.0, 1.0);
	} else {
		gl_Position = vec4((((in_vertices * translation.zw) * rotationMatrix)*vec2(1,1280.0/720.0)) + translation.xy * vec2(2.0, -2.0), 0.0, 1.0);
	}
		
	//if (translation.z == -1.0) {
	//	gl_Position = vec4((vec2((in_vertices*rotationMatrix) + centering)*translation.zw) + translation.xy * vec2(2.0, -2.0), 0.0, 1.0);
	//} else {
	//	gl_Position = vec4(rotationMatrix * vec2(((in_vertices + centering)*translation.zw)) + vec2(translation.xy), 0.0, 1.0); //
	//}
}
