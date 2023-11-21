import numpy as np


def export_anim(kwargs):
    parent = kwargs["node"]
    params = parent.parms()

    file = params[0].eval()
    start = params[1].eval()
    end = params[2].eval()

    # print(file, start, end)

    node = hou.pwd()
    input = node.inputs()[0]
    geo = input.geometry()

    # Open the CSV file for appending
    with open(file, "wb") as binfile:
        for i in range(start, end + 1):
            hou.setFrame(i)

            # Set a status message to display the progress
            progress = (i - start + 1) / (end - start + 1) * 100
            hou.ui.setStatusMessage(f"Exporting frame {i} ({progress:.2f}%)")

            # points = geo.points()[: 2**14]
            points = geo.points()

            length = np.uint16(len(points))
            # print(bytes(test)[:0])
            binfile.write(length)

            # Iterate through points
            for point in points:
                pos = point.position()

                color = [clamp(value * 255, 0, 255) for value in point.attribValue("Cd")]

                pscale = 64.0
                try:
                    pscale = point.attribValue("pscale") * 255 * 4  # last number is just an arbitrary scalar
                except:
                    pass

                # Write data to binary file
                for i in pos:
                    binfile.write(np.float16(i))

                for i in range(2, -1, -1):
                    binfile.write(np.uint8(color[i]))

                binfile.write(np.uint8(pscale))  # scale is alpha of color (ARGB)
                # 3*2 bytes position float16, 3*1 bytes color uint8, 1byte pscale uint8
                # total 10bytes per particle


# from stackoverflow
def clamp(val, minval, maxval):
    if val < minval:
        return minval
    if val > maxval:
        return maxval
    return val
